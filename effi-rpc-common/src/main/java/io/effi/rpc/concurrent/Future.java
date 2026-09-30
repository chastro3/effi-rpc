package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Defines an asynchronous result whose pending state is explicit through {@link #completed()}.
 */
public interface Future<T> {

    /**
     * Indicates whether this future has reached a terminal state.
     */
    boolean completed();

    /**
     * Registers a callback invoked with the terminal result.
     *
     * @param callback completion callback
     * @return this future
     */
    Future<T> onComplete(Consumer<Result<T>> callback);

    /**
     * Registers a callback invoked with the terminal result through an executor.
     *
     * @param executor callback executor
     * @param callback completion callback
     * @return this future
     */
    Future<T> onCompleteAsync(Executor executor, Consumer<Result<T>> callback);

    /**
     * Returns a stage completed with the terminal result.
     */
    CompletionStage<Result<T>> completion();

    /**
     * Waits for completion and returns the terminal result.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    Result<T> await() throws InterruptedException;

    /**
     * Waits until the deadline and returns the terminal result.
     *
     * @param deadline wait deadline
     * @return terminal result
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    Result<T> await(Deadline deadline) throws InterruptedException;

    /**
     * Attempts to cancel this future.
     *
     * @param reason cancellation reason
     * @return {@code true} when this call transitioned the future to cancelled
     */
    boolean cancel(EffiRpcException reason);

    /**
     * Returns a {@link CompletableFuture} adapted from this future.
     * <p>
     * Cancelling the returned future propagates cancellation back to this future.
     */
    default CompletableFuture<T> toCompletableFuture() {
        CompletableFuture<T> adapted = new CompletableFuture<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = super.cancel(mayInterruptIfRunning);
                if (cancelled) {
                    Future.this.cancel(PredefinedErrorCode.CALL_CANCELLED.fail("CompletableFuture was cancelled"));
                }
                return cancelled;
            }
        };
        onComplete(result -> {
            if (result.failed()) {
                adapted.completeExceptionally(result.cause());
            } else {
                adapted.complete(result.value());
            }
        });
        return adapted;
    }
}
