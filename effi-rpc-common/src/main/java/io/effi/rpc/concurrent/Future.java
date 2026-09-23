package io.effi.rpc.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Future<T> extends Result<T> {

    boolean completed();

    Future<T> onComplete(Consumer<Result<T>> handler);

    Future<T> timeout(long delay, TimeUnit unit);

    CompletableFuture<T> toCompletableFuture();

    /**
     * Attempts to cancel this future.
     *
     * @param mayInterruptIfRunning whether the operation may be interrupted
     * @return {@code true} if cancellation was accepted
     */
    default boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
}
