# Effi RPC Production Readiness

## Current Status (2026-10-09)

**Production-capable for the supported HTTP + registry deployment scope. gRPC is not production-ready.**

This document replaces the original 2026-09-23 audit baseline. Its checklist reflects the
current code and test state, not the historical gaps. The only unresolved launch items are
listed under [Remaining Before Production Launch](#remaining-before-production-launch).

Verification:

- `:effi-rpc-test:test --rerun-tasks --no-daemon` completed with `BUILD SUCCESSFUL`.
- 54 suites, 197 tests, 0 failures, 0 errors, 2 skipped.
- The 2 skipped tests are the manual `ApiTest` cases that require an external Consul
  registry or a manual stress run.
- Gradle `9.8.0`, JDK `25.0.3`.

### Completed

All P0 blockers from the original audit are resolved at the behavior level. The remaining
infrastructure metric sub-item of PR-06 is tracked under
[Remaining Before Production Launch](#remaining-before-production-launch).

- PR-01 real retry with bounded attempts, backoff, jitter, and deadline enforcement.
- PR-02 timeout and cancellation cleanup through an atomic call-attempt state machine.
- PR-03 startup readiness, bind/registration serialization, and rollback on startup failure.
- PR-04 platform-owned scheduler lifecycle with side-effect-free `active()`.
- PR-05 immutable discovery snapshots with stale instance replacement.
- PR-06 write-buffer watermarks, non-writable channel rejection, and an explicit
  `SERVER_OVERLOADED` response for rejected servant tasks.
- PR-07 TLS hostname verification and SNI.
- PR-08 deterministic event dispatch through `EventBus` / `MpscEventBus`.
- PR-09 registry heartbeat and retry task cancellation on close.
- PR-10 lifecycle-owned thread pools, schedulers, and event loops.

Other completed work:

- PR-11 immutable, ordered router configuration with URL plus service-instance metadata rules.
- PR-12 enabled the standard test task.
- PR-13 unified metrics kernel and metrics for calls, retries, timeouts, serialization,
  discovery, routing, load balancing, the event bus, named thread pools, connection pools,
  and server overload.

### Remaining Before Production Launch

1. Finish metrics coverage for infrastructure state:
   - `EffiRpcBootstrap.ready()` and `live()` exist, but readiness and liveness are not exposed
     as metric gauges.
   - Channel writability is not recorded as a metric.
   - Connection acquire duration and exact pending-acquire depth are not recorded. Netty
     `FixedChannelPool` does not expose a public pending-acquire count.
2. Run real-environment acceptance:
   - registry outage and recovery
   - bounded discovery timeout behavior
   - reconnect and connection-pool recovery under failure
   - soak and load testing
3. Keep gRPC streaming out of the production-supported scope until it is complete.
4. Optional tracing and request-ID propagation.

### Accepted Design Boundaries

- `Locator.locate(...)` remains synchronous by design. Service discovery is bounded by
  `GovernanceOptions.SERVICE_DISCOVERY_TIMEOUT`, intersected with the caller timeout. No
  asynchronous locator or stage-chain refactor is planned.
- `Invocation` is caller-side only. The server resolves `Object[] args` before constructing
  `CallContext`; there is no server-side intermediate `Invocation`.

## Blocker Status

### [x] PR-01: `failRetry` does not retry

Resolved.

`CallExecution` now owns the logical call lifecycle. It re-dispatches the original invocation
while the failure handler reports that the failure is retryable and the attempt budget remains.
Backoff and jitter come from `FaultToleranceOptions`; the execution deadline and cancellation
state stop further attempts.

Evidence:

- `effi-rpc-core/src/main/java/io/effi/rpc/core/call/CallExecution.java`
- `effi-rpc-core/src/main/java/io/effi/rpc/core/call/failure/FailRetry.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/context/CallExecutionTest.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/core/call/failure/FailRetryTest.java`

Related coverage: retry-until-success, exhaustion with the original failure, deadline
cancellation, explicit cancellation, and retry metrics.

### [x] PR-02: Timeout and cancellation do not clean up call state

Resolved.

`CallAttempt` atomically owns `ACQUIRING`, `ACTIVE`, `DONE`, and `CANCELLED`. Terminal completion
removes the future from the registry exactly once. Timeout during acquisition cancels the
acquire and releases a late channel without writing; timeout after send closes or resets the
active channel. HTTP/1 pooled connections and HTTP/2 streams use the same channel abstraction
for cleanup.

Evidence:

- `effi-rpc-core/src/main/java/io/effi/rpc/core/call/CallAttempt.java`
- `effi-rpc-core/src/main/java/io/effi/rpc/core/call/CallExecution.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/core/call/CallAttemptTest.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/context/CallExecutionTest.java`

Related coverage: timeout while acquiring closes the late channel without sending, timeout
after send closes the active channel, deadline cancels the active attempt, and explicit
cancellation stops execution. A concurrent response-versus-timeout race and repeated-timeout
load test remain part of the real-environment acceptance run.

### [x] PR-03: Application start reports success before bind and registration finish

Resolved.

`EffiRpcBootstrap.start()` returns `Future<Void>`. `ApplicationServiceRegistrar` runs bind and
registration as one state machine (`NEW`, `STARTING`, `READY`, `STOPPING`, `CLOSED`), rolls
back registration and server binding on failure, and never becomes `READY` after a concurrent
close. Readiness is exposed separately from process liveness.

Evidence:

- `effi-rpc-core/src/main/java/io/effi/rpc/core/EffiRpcBootstrap.java`
- `effi-rpc-core/src/main/java/io/effi/rpc/core/ApplicationServiceRegistrar.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/core/ApplicationServiceRegistrarTest.java`

Related coverage: bind failure keeps the registrar inactive, start and registration failures
close the application, and registering after close fails. Close during an in-flight startup
and full rollback of a partially bound server set remain part of the real-environment
acceptance run.

### [x] PR-04: Shared `Scheduler` can be closed by one platform and break others

Resolved.

`Scheduler` is a platform-scoped singleton. Each platform owns its scheduler instance and its
optional disposable/periodic executors. `active()` is side-effect free, `close()` is idempotent,
and shutdown is bounded. The lazy default executor is only created on first use.

Evidence:

- `effi-rpc-component/src/main/java/io/effi/rpc/component/tools/Scheduler.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/component/tools/SchedulerTest.java`

Related coverage: closing one scheduler does not affect another. Side-effect-free `active()`
and owned task cancellation follow from the platform ownership and lazy executor layout.

### [x] PR-05: Service discovery keeps stale instances and mutates shared lists

Resolved.

`DiscoveredService` stores an immutable snapshot through `List.copyOf(instances)` and replaces
the whole snapshot on every update. Consumers observe stable `Future` snapshots instead of a
mutable list. Closed clients fail lookups instead of mutating discovery state.

Evidence:

- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/AbstractRegistryClient.java`
- `effi-rpc-governance/src/main/java/io/effi/rpc/governance/registry/DefaultServiceDiscovery.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/registry/AbstractRegistryClientTest.java`

Related coverage: a subscription update replaces the snapshot and removes instances absent
from the new list, and repeated lookups reuse a stable snapshot future.

Blocking discovery inside the call path is an accepted boundary, not an open blocker. It is
bounded by the configured discovery timeout.

### [x] PR-06: No backpressure or overload response

Core blocker resolved. The infrastructure metric sub-item remains open.

Netty channels now configure write-buffer low/high watermarks. `NettyChannel` rejects writes
when the channel is not writable with `CHANNEL_OVERLOADED`. `ServerRequestHandler` converts
rejected servant tasks into a retryable `SERVER_OVERLOADED` response instead of silently
logging and letting the client time out.

Evidence:

- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/NettyChannel.java`
- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/NettySupport.java`
- `effi-rpc-transport/effi-rpc-transport-api/src/main/java/io/effi/rpc/transport/ServerRequestHandler.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/transport/options/TransportOptions.java`

Remaining: channel-writability is not recorded as a metric. Tracked under
[Remaining Before Production Launch](#remaining-before-production-launch).

### [x] PR-07: TLS does not verify the remote hostname or set SNI

Resolved.

Client SSL handlers are created with the actual remote host and port, which enables SNI, and
set `endpointIdentificationAlgorithm=HTTPS` on the SSL engine. Server and client contexts
remain separately cached by protocol and certificate configuration.

Evidence:

- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/EndpointChannelConfigurer.java`
- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/SslContextManager.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/transport/netty/EndpointChannelConfigurerTest.java`

Related coverage: the client handler receives host and port and sets HTTPS endpoint
identification. Wrong-host certificate rejection and mutual TLS remain real-environment
acceptance checks.

### [x] PR-08: Event dispatch has unsafe cross-thread propagation

Resolved.

The Disruptor listener-map design was replaced by `EventBus` / `MpscEventBus`. Control and
telemetry events use independent lanes; control events preserve order and block when their
lane is full; telemetry events may be sharded and dropped under load. Propagation flags are no
longer used, and handler chains are resolved per consumer lane.

Evidence:

- `effi-rpc-component/src/main/java/io/effi/rpc/component/event/EventBus.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/event/MpscEventBus.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/component/event/MpscEventBusTest.java`

Related coverage: dispatch to multiple handlers, handler-failure isolation, parent event
matching, control/telemetry lane behavior, backpressure, and shutdown draining.

### [x] PR-09: Registry heartbeat tasks survive client close

Resolved.

`AbstractRegistryClient` retains every scheduled heartbeat and registration-retry task in
`scheduledTasks`. `close()` cancels and clears them before deregistration. Scheduling checks
the closed flag before and after submission, so a heartbeat cannot be installed after close.

Evidence:

- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/AbstractRegistryClient.java`
- `effi-rpc-registry/effi-rpc-registry-api/src/main/java/io/effi/rpc/registry/RegisterTask.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/registry/AbstractRegistryClientTest.java`

Related coverage: closing the client clears queued heartbeat tasks and shuts down an owned
thread pool. Repeated create/close cycle verification remains part of the soak acceptance run.

### [x] PR-10: Threads and event loops leak across lifecycle boundaries

Resolved.

Default caller and servant thread pools are registered as platform-scoped `ThreadPool`
components, so platform close shuts them down with a bounded wait. `RpcThreadPool` no longer
keeps a static strong set of executors. The client event loop group is registered as an
external platform component with a shutdown hook. `ScopedContext.close()` releases initialized
components even when the context never started.

Evidence:

- `effi-rpc-core/src/main/java/io/effi/rpc/core/configurator/DefaultThreadPoolResolver.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/tools/ThreadPool.java`
- `effi-rpc-common/src/main/java/io/effi/rpc/executor/RpcThreadPool.java`
- `effi-rpc-component/src/main/java/io/effi/rpc/component/ScopedContext.java`
- `effi-rpc-transport/effi-rpc-transport-netty/src/main/java/io/effi/rpc/transport/netty/ClientEventLoopGroupRegister.java`

Related coverage: owned registry thread pools shut down on close, the scheduler closes only
its own executors, and component release runs even when a context was initialized but never
started. Full thread-leak verification belongs to the soak/load acceptance run.

## Required Before Production Launch

### [x] PR-11: Router configuration

`RouterConfig` is an immutable, ordered rule list; URL patterns are compiled once at build
time. Each rule combines a request-URL pattern with all-matching `ServiceInstance` metadata
conditions. `DefaultRouter` applies the first URL-matching rule and passes candidates through
when no rule matches. Routing no longer depends on the removed caller `group` option; tests
cover first-match and pass-through behavior.

Evidence:

- `effi-rpc-governance/src/main/java/io/effi/rpc/governance/router/RouterConfig.java`
- `effi-rpc-governance/src/main/java/io/effi/rpc/governance/router/DefaultRouter.java`
- `effi-rpc-test/src/test/java/io/effi/rpc/governance/registry/RegistryLocatorRouteTest.java`

### [x] PR-12: Automated tests enabled

The standard `:effi-rpc-test:test` task is enabled and passes locally. Only two manual
`ApiTest` cases remain disabled.

Real-environment acceptance scenarios still need to be run before claiming registry outage,
reconnect, and long-running soak stability.

### [x] PR-13: Core production observability

The metrics kernel is in place, and metrics are registered for:

- caller calls, failures, retries, timeouts, and call duration
- servant requests, failures, and request duration
- serialization and deserialization duration on both sides
- governance discovery, routing, and load-balancer selection
- event-bus publish, delivery, failure, and pending-count gauge
- named thread-pool active, queue-size, and rejected-task metrics
- connection-pool acquired, max, and acquire-failure metrics
- servant overload count

Readiness and liveness are exposed as `EffiRpcBootstrap.ready()` and `live()`.

Infrastructure-state metrics remain open and are listed under
[Remaining Before Production Launch](#remaining-before-production-launch).

## Recommended Next Steps

1. Close the remaining observability items: readiness/liveness gauges, channel-writability
   counter, and connection acquire duration.
2. Run real-environment registry outage/recovery, bounded discovery timeout, reconnect, and
   soak/load acceptance.
3. Keep gRPC streaming out of the supported production scope until it is complete.

Infrastructure metrics completion and real-environment verification are the remaining launch
gates. The supported HTTP and registry deployment model is production-capable from the code
and test state above.
