package io.effi.rpc.component.support;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.trait.Closeable;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages named thread pools that wrap {@link ExecutorService} instances.
 * <p>
 * Provides lifecycle management and asynchronous execution capabilities
 * for thread pools.
 */
@ScopedComponent(scope = PLATFORM)
public record ThreadPool(String id, ExecutorService executor) implements Closeable, Identifiable {

    public ThreadPool(String id, ExecutorService executor) {
        this.id = AssertUtil.notBlank(id, "id");
        this.executor = AssertUtil.notNull(executor, "executor");
    }

    public <T> Future<T> execute(Supplier<T> supplier) {
        Promise<T> promise = new Promise<>();
        executor.submit(() -> {
            try {
                T result = supplier.get();
                promise.success(result);
            } catch (Throwable e) {
                promise.failure(e);
            }
        });
        return promise;
    }

    public Future<Void> execute(Runnable task) {
        Promise<Void> promise = new Promise<>();
        executor.submit(() -> {
            try {
                task.run();
                promise.success(null);
            } catch (Throwable e) {
                promise.failure(e);
            }
        });
        return promise;
    }

    @Override
    public boolean active() {
        return !executor.isShutdown();
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
