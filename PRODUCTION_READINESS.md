# Effi RPC Production Readiness Blockers

## Status Update (2026-09-28)

The sections below are the original audit baseline and are kept for history.
Their status is superseded by this summary.

Completed:

- Startup readiness and shutdown race protection.
- Scheduler and default thread-pool ownership.
- TLS hostname verification and SNI.
- Netty write-buffer watermarks and overload responses.
- Registry heartbeat cancellation and immutable discovery snapshots.
- Retrying only explicitly transient failures, with exponential backoff and jitter.
- Group and router-rule routing.
- Concurrent listener registration and removal.
- Enabled the standard test task; only two manual `ApiTest` cases remain disabled.
- Basic timeout, thread-pool, connection-pool, readiness, and liveness metrics.

Remaining architectural item:

- `Locator.locate(...)` is synchronous, so service discovery can still block
  its calling thread until the registry future completes. Removing this requires
  making the locator contract and stage chain asynchronous; the snapshot,
  timeout, and stale-instance problems are already fixed.

Verification:

- `.\gradlew.bat build --no-daemon --no-configuration-cache`
- Completed successfully with 59 Gradle tasks, including the enabled test task.

Baseline commit: `207b91c` (`feat: harden dynamic accessor generation`)

Audit date: `2026-09-23`

Current status: **not production stable**

Baseline verification: `.\gradlew.bat build -x test --no-daemon --no-configuration-cache`
passed with 57 tasks. The integration test task is currently disabled, so this build does not
exercise timeout, retry, reconnect, registry failure, or shutdown behavior.

Do not mark a blocker complete without adding or running the specified acceptance tests.

## P0 Blockers

### [ ] PR-01: `failRetry` does not retry

Location:

- `effi-rpc-context/src/main/java/io/effi/rpc/context/support/failure/FailRetry.java`

Problem:

- When the retry count is not exhausted, `handle()` logs and returns.
- It does not schedule another invocation and does not complete the current `ReplyFuture`.
- The caller therefore waits until the normal timeout, and no retry occurs.

Impact:

- Configured fault tolerance is ineffective.
- Failures are reported as timeouts rather than retry exhaustion.
- Request latency and resource usage increase without recovery benefit.

Required fix:

- Implement retry as a real new call attempt with a bounded attempt counter.
- Preserve the original request arguments, call context, timeout budget, and cancellation state.
- Complete the original future on success or final retry exhaustion.
- Add jitter/backoff and make retry policy configurable.
- If retry cannot be implemented immediately, remove `failRetry` from usable production
  configuration or fail validation when it is selected.

Acceptance tests:

- First attempts fail and a later attempt succeeds; the caller receives the successful result.
- Retry exhaustion completes with the original failure.
- The total retry budget respects the call timeout.
- Cancellation or timeout stops future retry attempts.

### [x] PR-02: Timeout and cancellation do not clean up call state

Status: core fix implemented.

Implemented:

- `ReplyFuture` now has a one-shot terminal cleanup hook and cancellation actions.
- `FUTURES` removal is centralized in the terminal hook, including timeout completion.
- `Promise.cancel()` can propagate cancellation to non-Netty futures.
- Netty acquire promises propagate cancellation to the underlying Netty future.
- `CallAttempt` atomically owns `ACQUIRING`, `ACTIVE`, `DONE`, and `CANCELLED` state.
- A channel that arrives after timeout is closed without sending a request.
- A channel active when timeout wins is closed, covering HTTP/1 pool eviction and HTTP/2
  stream cancellation through the `Channel` abstraction.
- `FutureResultStage` delegates acquire/send failure and cleanup to `CallAttempt`.

Verification:

- `CallAttemptTest.timeoutWhileAcquiringClosesLateChannelWithoutSending`
- `CallAttemptTest.timeoutAfterSendClosesActiveChannel`
- Both tests pass with a temporary Gradle init script while the repository-wide test task
  remains disabled.

Remaining integration acceptance:

- Exercise timeout through a real `Http1Client`/`FixedChannelPool` and assert pool recovery.
- Exercise timeout through a real `Http2Client` and assert the stream closes while the physical
  connection remains reusable.
- Add a response-versus-timeout concurrent race test when the normal test task is restored.

Location:

- `effi-rpc-context/src/main/java/io/effi/rpc/context/ReplyFuture.java`
- `effi-rpc-boot/src/main/java/io/effi/rpc/boot/confiurator/stage/FutureResultStage.java`
- `effi-rpc-protocols/effi-rpc-http/src/main/java/io/effi/rpc/protocol/http/FutureBinder.java`
- `effi-rpc-protocols/effi-rpc-http/src/main/java/io/effi/rpc/protocol/http/h1/Http1ClientHandler.java`
- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/NettyChannel.java`

Current problem:

- `ReplyFuture` is registered in the static `FUTURES` map in its constructor.
- `FUTURES.remove(id)` only runs from `complete()` and `failure()`.
- Timeout completion calls `AbstractFuture.tryComplete()` directly, so the entry remains forever.
- `FutureResultStage` continues the acquire/send flow even if the future timed out while waiting
  for a channel.
- HTTP/1 channels are released to the pool only after a response arrives. A timeout or missing
  response leaves the channel bound and unavailable.
- HTTP/2 stream channels are not explicitly reset or closed on timeout.
- There is no shared cancellation token connecting caller timeout, channel acquisition, write,
  response handling, and cleanup.

Impact:

- Permanent `FUTURES` memory growth for every timed-out call.
- HTTP/1 fixed channel pool exhaustion after repeated timeouts.
- Requests can be sent after their caller has already timed out.
- Late responses and in-flight writes can race with reused pooled connections.
- HTTP/2 streams can remain open after caller completion.

Recommended solution:

Use one explicit per-call lifecycle object with an atomic state machine and a cancellation
token. The future is the caller-visible result; the lifecycle object owns all transport cleanup.

```text
ACQUIRING -> SENT -> COMPLETED
    |         |
    +-------> CANCELLED
```

Design requirements:

1. Add `ReplyFuture.onCancel(Runnable)` or an equivalent cancellation-token API.
2. Override the terminal completion path in `ReplyFuture` so every terminal state removes the
   global `FUTURES` entry exactly once:
   - success
   - remote failure
   - write failure
   - timeout
   - explicit cancellation
3. `FutureResultStage` must register cleanup before starting channel acquisition.
4. Store the pending Netty channel-acquire future.
   - On timeout while `ACQUIRING`, cancel the acquire.
   - If the acquire races with cancellation and succeeds later, close or release the returned
     channel immediately and never write the request.
5. On timeout while `SENT`:
   - HTTP/1: close the leased connection. Do not return an in-flight request to the pool.
   - HTTP/2: reset/close the stream channel while keeping the physical connection reusable.
   - Unbind the Netty future ID and clear any request/response stream attributes.
6. Make cleanup idempotent. A late response, timeout, and write failure can race, but channel
   release/close and map removal must each happen at most once.
7. Check `future.completed()` immediately before writing. This is a safety check, not a
   replacement for cancellation.
8. Remove `FUTURES` entries from a terminal hook rather than duplicating removal logic in each
   public method.

Suggested shape:

```java
CallAttempt attempt = new CallAttempt(future);
future.onCancel(attempt::cancel);

client.fetchChannel().onComplete(res -> {
    if (future.completed()) {
        attempt.releaseUnused(res);
        return;
    }
    attempt.send(res);
});
```

`cancel()` must be atomic:

```text
ACQUIRING -> CANCELLED: cancel acquire; release a raced channel if one arrives
SENT -> CANCELLED: close/reset the active channel or stream
COMPLETED -> CANCELLED: no-op
```

Acceptance tests:

- Timeout before channel acquisition: no request is written; a late acquired channel is released
  or closed.
- Timeout after write with no response: the HTTP/1 channel is closed, the pool returns to its
  configured active count, and the future map returns to its baseline size.
- HTTP/2 timeout resets the stream without closing the physical pooled connection.
- A response arriving after timeout is ignored and does not release or close a channel twice.
- A response and timeout racing concurrently perform cleanup exactly once.
- Repeated timeout load does not increase `FUTURES` size or exhaust the HTTP/1 pool.

### [ ] PR-03: Application start reports success before bind and registration finish

Location:

- `effi-rpc-boot/src/main/java/io/effi/rpc/boot/InitializedConfiguration.java`
- `effi-rpc-boot/src/main/java/io/effi/rpc/boot/ApplicationServiceRegistrar.java`
- `effi-rpc-boot/src/main/java/io/effi/rpc/boot/EffiRpcBootstrap.java`

Problem:

- `ApplicationInitializedListener.onStarted()` calls `register()` and ignores the returned
  future.
- `EffiRpcBootstrap.start()` can return while bind or registry registration is still pending.
- A close during `starting` can race with the asynchronous registration and allow the registrar
  to become active after shutdown has begun.

Impact:

- An application can be reported healthy before its port is listening or it is registered.
- Kubernetes or other supervisors may route traffic to a non-ready instance.
- Failed startup can appear successful and leave partially initialized resources.

Required fix:

- Make start return a `Future<Void>` or provide an explicit `startAsync()` plus `awaitReady()`.
- Run bind and registration as one startup state machine.
- On startup failure, roll back bind, registration, active state, and owned resources.
- Make cancellation/close transition `STARTING` to `CANCELLING`; a later startup completion must
  never set `active=true`.
- Expose readiness separately from process liveness.

Acceptance tests:

- Port bind failure makes startup fail and leaves the registrar inactive.
- Registry failure triggers rollback and process readiness remains false.
- Close during bind or registration completes cancellation without leaving a server running.
- Successful startup completes only after bind and all required registrations succeed.

### [ ] PR-04: Shared `Scheduler` can be closed by one platform and break others

Location:

- `effi-rpc-component/src/main/java/io/effi/rpc/component/support/Scheduler.java`

Problem:

- All `Scheduler` instances fall back to one static `DEFAULT_SCHEDULER`.
- `close()` shuts down that shared executor.
- `active()` calls lazy getters and can initialize the executor while only querying state.

Impact:

- Closing one platform disables scheduling for every platform in the same JVM.
- New timers can fail with `RejectedExecutionException` after another platform closes.
- A read-only lifecycle query mutates and allocates resources.

Required fix:

- Give each platform an owned scheduler lifecycle, or use reference-counted shared ownership.
- Make `active()` side-effect free.
- Cancel owned tasks during close and await bounded termination.
- Do not shut down a global executor from an individual `Scheduler.close()`.

Acceptance tests:

- Two platforms can schedule independently; closing one does not affect the other.
- `active()` does not initialize a scheduler.
- Tasks owned by a closed platform are cancelled.

### [ ] PR-05: Service discovery keeps stale instances and mutates shared lists

Location:

- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/AbstractRegistryClient.java`
- `effi-rpc-common/src/main/java/io/effi/rpc/util/CollectionUtil.java`
- `effi-rpc-governance/src/main/java/io/effi/rpc/governance/registry/DefaultServiceDiscovery.java`

Problem:

- `DiscoveredService.update()` only replaces matching IDs and never removes instances absent from
  the latest registry snapshot.
- The promise exposes a read-only wrapper around a list that is still mutated by subscription
  callbacks.
- Discovery uses blocking `CompletableFuture.get()` from the request thread.

Impact:

- Traffic continues to be routed to deregistered or unhealthy instances.
- Concurrent refresh and lookup can throw `ConcurrentModificationException`.
- Registry latency consumes request worker threads.

Required fix:

- Store discovery state as an immutable snapshot:
  `instanceRef.set(List.copyOf(instances))`.
- Replace the whole snapshot on every update instead of editing the previous list.
- Return stable immutable snapshots to consumers.
- Prefer asynchronous composition; if blocking is temporarily retained, isolate it from request
  threads and document the timeout budget.

Acceptance tests:

- Updating from `[A, B]` to `[A, C]` removes `B`.
- Concurrent refresh and lookup never mutate a list being iterated.
- An empty healthy snapshot does not remain populated by stale entries.

### [ ] PR-06: No backpressure or overload response

Location:

- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/NettyChannel.java`
- `effi-rpc-transport/effi-rpc-transport-api/src/main/java/io/effi/rpc/transport/TransportSupport.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/transport/options/TransportOptions.java`

Problem:

- Every outbound message calls `writeAndFlush` without checking `Channel.isWritable()`.
- There is no write-buffer watermark configuration.
- Rejected server thread-pool tasks are logged but no overload response is sent.

Impact:

- Slow consumers can create unbounded Netty outbound queues and OOM the process.
- Thread-pool saturation turns into client-side timeouts rather than an immediate overloaded
  response.

Required fix:

- Configure Netty write-buffer watermarks.
- Check writability and apply bounded enqueueing, waiting for writability, or immediate
  rejection.
- Map server rejection to an explicit overloaded/unavailable RPC error.
- Add queue, active task, rejection, and channel-writability metrics.

Acceptance tests:

- Slow peer does not create unbounded writes.
- Saturation returns an overloaded error within a bounded time.
- Metrics expose queue depth and rejection count.

### [ ] PR-07: TLS does not verify the remote hostname or set SNI

Location:

- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/EndpointChannelConfigurer.java`
- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/SslContextManager.java`

Problem:

- Client channels call `sslContext.newHandler(alloc)` without peer host and port.
- The SSL engine does not set `endpointIdentificationAlgorithm=HTTPS`.
- SNI and hostname verification are therefore incomplete.

Impact:

- A certificate signed by a trusted CA but issued for another hostname may be accepted.
- This enables man-in-the-middle attacks against production RPC traffic.

Required fix:

- Create client SSL handlers with the actual host and port.
- Set the HTTPS endpoint identification algorithm.
- Enable SNI.
- Separate one-way TLS and mutual TLS certificate configuration.

Acceptance tests:

- Wrong-host certificate is rejected.
- SNI selects the correct virtual-host certificate.
- Mutual TLS works only when explicitly configured.

### [x] PR-08: Event dispatch has unsafe cross-thread propagation

Location:

- `effi-rpc-component/src/main/java/io/effi/rpc/component/event/DisruptorEventDispatcher.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/event/AbstractEvent.java`

Problem:

- Multiple parallel Disruptor handlers share the same listener map.
- `event.stopPropagation()` is used as a cross-thread claim, then reset inside a listener
  `finally` block.
- This does not provide exactly-once processing or reliable propagation semantics.

Impact:

- Metrics may be counted more than once.
- Idle events may execute multiple actions.
- Concurrent listener registration and dispatch can produce inconsistent behavior.

Resolution:

- Replaced the Disruptor listener-map design with `EventBus` / `MpscEventBus`.
- Control and telemetry events use independent lanes; control events preserve order and
  block when their lane is full.
- Telemetry events may be sharded across consumers and drop under load.
- Handler chains are resolved per consumer lane; propagation flags are no longer used.
- Removed the production Disruptor dependency.
- If work distribution is required, use a worker pool with a defined partition key.

Acceptance tests:

- One published event invokes each listener exactly once under concurrent dispatch.
- Listener exceptions do not alter propagation for unrelated handlers.

### [ ] PR-09: Registry heartbeat tasks survive client close

Location:

- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/AbstractRegistryClient.java`
- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/RegisterTask.java`

Problem:

- `addPeriodic(registerTask, ...)` does not retain the scheduled task handle.
- `close()` clears registration state and closes the backend, but the periodic task remains.

Impact:

- A closed client can continue touching a closed registry connection.
- Services may be re-registered after graceful shutdown.
- Repeated create/close cycles retain tasks and client instances.

Required fix:

- Retain and cancel every scheduled registration/health task on close.
- Make tasks check the client closed flag before registration.
- Ensure deregistration and cancellation complete in a deterministic order.

Acceptance tests:

- After close, no backend call occurs from a former heartbeat task.
- Repeated close/create cycles leave no scheduled work.

### [ ] PR-10: Threads and event loops leak across lifecycle boundaries

Location:

- `effi-rpc-boot/src/main/java/io/effi/rpc/boot/confiurator/DefaultThreadPoolConfigurator.java`
- `effi-rpc-common/src/main/java/io/effi/rpc/executor/RpcThreadPool.java`
- `effi-rpc-common/src/main/java/io/effi/rpc/concurrent/AbstractFuture.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/ScopedContext.java`

Problem:

- Default caller/servant thread pools are created as non-daemon and are never registered as
  lifecycle-owned components.
- `RpcThreadPool` keeps a static strong set of executors.
- The default future scheduler is non-daemon and has no shutdown integration.
- `ScopedContext.close()` does nothing when `active=false`, even if initialized resources have
  already been created.

Impact:

- JVM or test processes may not exit.
- Application restart leaks thread pools.
- Initialized-but-not-started components can leak threads and buffers.

Required fix:

- Register all owned pools and schedulers with platform/application lifecycle ownership.
- Shut down and await termination with a bounded timeout.
- Remove executors from static tracking after shutdown.
- Make `close()` release initialized resources even if start never completed.

Acceptance tests:

- Start and close leave no non-daemon framework threads.
- Initialize without start, then close, releases created resources.
- Repeated lifecycle cycles do not accumulate threads or executors.

## Required Before Production Launch

### [ ] PR-11: Router configuration is incomplete

- `DefaultRouter` compares `caller.group` with `caller.group`, so the group filter is always true.
- `routerConfigs` is hardcoded to an empty list, so router rules are never applied.
- Load and filter against instance metadata, compile patterns once, and test empty results.

### [ ] PR-12: Automated tests are globally disabled

- `effi-rpc-test/build.gradle.kts` sets `tasks.test.enabled = false`.
- Enable the test task and isolate only the known hanging tests with timeouts or `@Disabled`.
- Required suites: timeout cleanup, retry, registry outage, stale discovery, pool exhaustion,
  reconnect, shutdown, backpressure, TLS hostname verification, and event exactly-once delivery.

### [ ] PR-13: Production observability is incomplete

- Add readiness/liveness state.
- Add connection pool active/idle/acquired counts.
- Add thread-pool queue depth, active count, and rejection count.
- Add timeout, cancellation, retry, registry refresh failure, and late-response counters.
- Add tracing/request ID propagation and structured lifecycle logs.

## Recommended Fix Order

1. PR-02 timeout/cancellation state machine.
2. PR-01 real retry.
3. PR-03 startup readiness and rollback.
4. PR-06 connection/write backpressure.
5. PR-04, PR-09, and PR-10 lifecycle ownership.
6. PR-05 discovery snapshots and PR-11 routing.
7. PR-07 TLS verification.
8. PR-08 event dispatch correctness (resolved by `EventBus` / `MpscEventBus`).
9. PR-12 tests and PR-13 observability.

Production stability should not be claimed until PR-01 through PR-10 are complete and the
acceptance tests are running in the standard build.
