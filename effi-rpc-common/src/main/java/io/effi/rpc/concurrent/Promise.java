package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Provides a thread-safe, single-assignment {@link Future} implementation.
 */
public final class Promise<T> implements Future<T> {

    private static final Logger logger = LoggerFactory.getLogger(Promise.class);

    private final AtomicReference<Result<T>> result = new AtomicReference<>();

    private final CompletableFuture<Result<T>> completion = new CompletableFuture<>();

    private final ConcurrentLinkedQueue<Callback<T>> callbacks = new ConcurrentLinkedQueue<>();

    private final Object cancellationLock = new Object();

    private final List<Consumer<EffiRpcException>> cancellationHandlers = new ArrayList<>();

    private boolean cancellationRequested;

    private EffiRpcException cancelReason;

    private boolean terminal;

    /**
     * Returns an already completed promise.
     *
     * @param value success value
     * @return completed promise
     */
    public static <T> Promise<T> completed(T value) {
        Promise<T> promise = new Promise<>();
        promise.complete(Result.success(value));
        return promise;
    }

    /**
     * Returns an already failed promise.
     *
     * @param cause failure cause
     * @return failed promise
     */
    public static <T> Promise<T> failed(EffiRpcException cause) {
        Promise<T> promise = new Promise<>();
        promise.complete(Result.failure(cause));
        return promise;
    }

    /**
     * Completes this promise with a terminal result.
     * <p>
     * The first completion attempt wins; later attempts return {@code false}.
     *
     * @param terminalResult terminal result
     * @return {@code true} when this call completed the promise
     */
    public boolean complete(Result<T> terminalResult) {
        AssertUtil.notNull(terminalResult, "terminalResult");
        if (!result.compareAndSet(null, terminalResult)) {
            return false;
        }
        synchronized (cancellationLock) {
            terminal = true;
            cancellationHandlers.clear();
        }
        publish(terminalResult);
        return true;
    }

    /**
     * Completes this promise successfully.
     *
     * @param value success value
     * @return {@code true} when this call completed the promise
     */
    public boolean success(T value) {
        return complete(Result.success(value));
    }

    /**
     * Completes this promise with a failure.
     *
     * @param cause failure cause
     * @return {@code true} when this call completed the promise
     */
    public boolean failure(EffiRpcException cause) {
        return complete(Result.failure(cause));
    }

    /**
     * Registers a cancellation handler.
     * <p>
     * The handler runs immediately when cancellation has already been requested.
     *
     * @param handler cancellation handler
     * @return this promise
     */
    public Promise<T> onCancel(Consumer<EffiRpcException> handler) {
        AssertUtil.notNull(handler, "handler");
        EffiRpcException reasonToRun = null;
        synchronized (cancellationLock) {
            if (terminal) {
                return this;
            }
            if (cancellationRequested) {
                reasonToRun = cancelReason;
            } else {
                cancellationHandlers.add(handler);
                return this;
            }
        }
        runCancellationHandler(handler, reasonToRun);
        return this;
    }

    @Override
    public boolean completed() {
        return result.get() != null;
    }

    @Override
    public Promise<T> onComplete(Consumer<Result<T>> callback) {
        return onCompleteAsync(null, callback);
    }

    @Override
    public Promise<T> onCompleteAsync(Executor executor, Consumer<Result<T>> callback) {
        AssertUtil.notNull(callback, "callback");
        Callback<T> entry = new Callback<>(executor, callback);
        Result<T> current = result.get();
        if (current != null) {
            dispatch(entry, current);
            return this;
        }
        callbacks.add(entry);
        // Re-check after enqueue to close the race with terminal publication.
        current = result.get();
        if (current != null && callbacks.remove(entry)) {
            dispatch(entry, current);
        }
        return this;
    }

    @Override
    public CompletionStage<Result<T>> completion() {
        return completion;
    }

    @Override
    public Result<T> await() throws InterruptedException {
        try {
            return completion.get();
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        }
    }

    /**
     * Waits until the deadline, cancelling this promise when the deadline elapses.
     *
     * @param deadline wait deadline
     * @return terminal result
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @Override
    public Result<T> await(Deadline deadline) throws InterruptedException {
        AssertUtil.notNull(deadline, "deadline");
        if (deadline.isNone()) {
            return await();
        }
        long remaining = deadline.remainingNanos();
        if (remaining <= 0L) {
            cancel(PredefinedErrorCode.DEADLINE_EXCEEDED.fail(deadline.remainingNanos()));
            return completion.join();
        }
        try {
            return completion.get(remaining, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            cancel(PredefinedErrorCode.DEADLINE_EXCEEDED.fail(0L));
            return completion.join();
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        }
    }

    @Override
    public boolean cancel(EffiRpcException reason) {
        AssertUtil.notNull(reason, "reason");
        Result<T> cancelled = Result.failure(reason);
        if (!result.compareAndSet(null, cancelled)) {
            return false;
        }
        List<Consumer<EffiRpcException>> handlers;
        synchronized (cancellationLock) {
            cancellationRequested = true;
            cancelReason = reason;
            handlers = List.copyOf(cancellationHandlers);
            cancellationHandlers.clear();
        }
        handlers.forEach(handler -> runCancellationHandler(handler, reason));
        publish(cancelled);
        return true;
    }

    private void publish(Result<T> terminalResult) {
        completion.complete(terminalResult);
        Callback<T> callback;
        while ((callback = callbacks.poll()) != null) {
            dispatch(callback, terminalResult);
        }
    }

    private void dispatch(Callback<T> callback, Result<T> terminalResult) {
        Executor executor = callback.executor();
        if (executor == null) {
            invoke(callback.callback(), terminalResult);
            return;
        }
        try {
            executor.execute(() -> invoke(callback.callback(), terminalResult));
        } catch (RejectedExecutionException e) {
            logger.warn("Callback executor rejected completion; running inline", e);
            invoke(callback.callback(), terminalResult);
        }
    }

    private void invoke(Consumer<Result<T>> callback, Result<T> terminalResult) {
        try {
            callback.accept(terminalResult);
        } catch (Throwable e) {
            logger.error("Future callback failed.", e);
        }
    }

    private void runCancellationHandler(Consumer<EffiRpcException> handler, EffiRpcException reason) {
        try {
            handler.accept(reason);
        } catch (Throwable e) {
            logger.error("Future cancellation handler failed.", e);
        }
    }

    private record Callback<T>(Executor executor, Consumer<Result<T>> callback) {
    }
}
