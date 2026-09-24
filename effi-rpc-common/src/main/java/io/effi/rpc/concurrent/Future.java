package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * A future is not itself a terminal result. Pending state is explicit via {@link #completed()}.
 */
public interface Future<T> {

    boolean completed();

    Future<T> onComplete(Consumer<Result<T>> callback);

    Future<T> onCompleteAsync(Executor executor, Consumer<Result<T>> callback);

    CompletionStage<Result<T>> completion();

    Result<T> await() throws InterruptedException;

    Result<T> await(Deadline deadline) throws InterruptedException;

    boolean cancel(EffiRpcException reason);

    /**
     * Adapts this future to {@link CompletableFuture}.
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
