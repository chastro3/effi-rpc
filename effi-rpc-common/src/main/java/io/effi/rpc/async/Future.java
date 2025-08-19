package io.effi.rpc.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface Future<T> extends Result<T> {

    boolean completed();

    Future<T> onComplete(Consumer<Result<T>> handler);

    Future<T> timeout(long delay, TimeUnit unit);

    CompletableFuture<T> toCompletableFuture();
}