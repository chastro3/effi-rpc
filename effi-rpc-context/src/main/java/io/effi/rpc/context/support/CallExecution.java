package io.effi.rpc.context.support;

import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;
import static io.effi.rpc.context.options.FaultToleranceOptions.RETRY_BACKOFF;
import static io.effi.rpc.context.options.FaultToleranceOptions.RETRY_JITTER;
import static io.effi.rpc.context.options.FaultToleranceOptions.RETRY_MAX_BACKOFF;

/**
 * Executes one logical unary call, including deadline control and retry attempts.
 */
public final class CallExecution<R> {

    private final Caller<R> caller;

    private final Invocation invocation;

    private final Unary.FailureHandler failureHandler;

    private final Promise<R> completion = new Promise<>();

    private final Deadline deadline;

    private final AtomicReference<ReplyFuture> attemptFuture = new AtomicReference<>();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final AtomicInteger failureCount = new AtomicInteger();

    private volatile ScheduledFuture<?> deadlineTask;

    public CallExecution(Caller<R> caller, Invocation invocation, Unary.FailureHandler failureHandler) {
        this.caller = caller;
        this.invocation = invocation;
        this.failureHandler = failureHandler;
        this.deadline = deadline(caller);
        this.completion.onCancel(this::onCancelled);
        this.completion.onComplete(ignored -> cancelDeadline());
    }

    /**
     * Starts this execution.
     *
     * @return the future that completes with the unary result
     */
    public Future<R> execute() {
        if (started.compareAndSet(false, true) && !cancelIfExpired()) {
            scheduleDeadline();
            dispatch();
        }
        return completion;
    }

    /**
     * Cancels this execution and its active attempt.
     *
     * @param reason the cancellation reason
     * @return {@code true} when the execution was cancelled
     */
    public boolean cancel(EffiRpcException reason) {
        return completion.cancel(reason);
    }

    private void dispatch() {
        if (cancelled.get() || completion.completed() || cancelIfExpired()) {
            return;
        }

        CallContext<Request, Caller<?>> context = newContext();
        MetricsSupport.recordStartTime(context);

        ReplyFuture attempt;
        try {
            attempt = invoke(context);
        } catch (Throwable cause) {
            onFailed(context, toRpcException(cause));
            return;
        }
        track(context, attempt);
    }

    private void track(CallContext<Request, Caller<?>> context, ReplyFuture attempt) {
        if (cancelled.get() || completion.completed() || !attemptFuture.compareAndSet(null, attempt)) {
            attempt.cancel(PredefinedErrorCode.CALL_CANCELLED.fail("call execution cancelled"));
            return;
        }
        attempt.onComplete(outcome -> onCompleted(context, attempt, outcome));
    }

    @SuppressWarnings("unchecked")
    private void onCompleted(
            CallContext<Request, Caller<?>> context,
            ReplyFuture attempt,
            Result<ReplyContext<Response, Caller<?>>> outcome
    ) {
        if (!attemptFuture.compareAndSet(attempt, null)) {
            return;
        }
        if (outcome.failed()) {
            onFailed(context, outcome.cause());
            return;
        }

        Interaction.Result result = attempt.rawResult();
        if (result == null) {
            completion.failure(InteractionErrorCodes.REPLY_RESULT_MISSING.fail(attempt.id()));
        } else if (result.failed()) {
            completion.failure(result.cause());
        } else {
            completion.success((R) result.value());
        }
    }

    private void onFailed(CallContext<Request, Caller<?>> context, EffiRpcException cause) {
        if (cancelled.get() || completion.completed() || cancelIfExpired()) {
            return;
        }

        int failures = failureCount.incrementAndGet();
        try {
            failureHandler.handle(context, failures, cause);
        } catch (EffiRpcException e) {
            completion.failure(e);
            return;
        }
        retry();
    }

    private void retry() {
        Scheduler scheduler = caller.platform().singleComponent(Scheduler.class);
        if (scheduler == null) {
            dispatch();
            return;
        }
        try {
            scheduler.addDisposable(this::dispatch, retryDelayMillis(), TimeUnit.MILLISECONDS);
        } catch (Throwable ignored) {
            // Fall back to inline retry when the scheduler is shutting down.
            dispatch();
        }
    }

    private long retryDelayMillis() {
        long delay = Math.max(0, caller.option(RETRY_BACKOFF));
        long max = Math.max(delay, caller.option(RETRY_MAX_BACKOFF));
        for (int i = 1; i < failureCount.get() && delay < max; i++) {
            delay = Math.min(max, delay * 2);
        }
        int jitter = Math.max(0, caller.option(RETRY_JITTER));
        return delay + (jitter == 0 ? 0 : ThreadLocalRandom.current().nextLong(jitter + 1L));
    }

    private void onCancelled(EffiRpcException reason) {
        cancelled.set(true);
        cancelDeadline();
        ReplyFuture attempt = attemptFuture.getAndSet(null);
        if (attempt != null) {
            attempt.cancel(reason);
        }
    }

    private boolean cancelIfExpired() {
        if (!deadline.expired()) {
            return false;
        }
        failDeadline();
        return true;
    }

    private void scheduleDeadline() {
        if (deadline.isNone() || completion.completed()) {
            return;
        }
        Scheduler scheduler = caller.platform().singleComponent(Scheduler.class);
        if (scheduler == null) {
            return;
        }
        deadlineTask = scheduler.addDisposable(
                this::onDeadline,
                deadline.remainingNanos(),
                TimeUnit.NANOSECONDS
        );
        if (completion.completed()) {
            cancelDeadline();
        }
    }

    private void onDeadline() {
        failDeadline();
    }

    private void failDeadline() {
        CallerMetrics metrics = caller.get(CallerMetrics.GENERIC_KEY);
        if (metrics != null) {
            metrics.timeoutCount().increment();
        }
        completion.cancel(PredefinedErrorCode.DEADLINE_EXCEEDED.fail(0L));
    }

    private void cancelDeadline() {
        ScheduledFuture<?> task = deadlineTask;
        deadlineTask = null;
        if (task != null) {
            task.cancel(false);
        }
    }

    private ReplyFuture invoke(CallContext<Request, Caller<?>> context) {
        Interaction.Result result = caller.callStageChain().proceed(context);
        return result.excepted();
    }

    private CallContext<Request, Caller<?>> newContext() {
        Request request = caller.protocol().createRequest(caller, invocation);
        return new CallContext<>(caller.module(), request, caller, Unary.MODE, invocation.arguments().values());
    }

    private static Deadline deadline(Caller<?> caller) {
        long timeout = caller.option(TIMEOUT);
        return timeout < 0
                ? Deadline.none()
                : Deadline.after(timeout, TimeUnit.MILLISECONDS);
    }

    private static EffiRpcException toRpcException(Throwable cause) {
        return cause instanceof EffiRpcException effiRpcException
                ? effiRpcException
                : InteractionErrorCodes.CALL_ATTEMPT_FAILED.fail(cause);
    }
}
