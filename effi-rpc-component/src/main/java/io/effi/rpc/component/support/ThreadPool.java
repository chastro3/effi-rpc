package io.effi.rpc.component.support;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.resoruce.Closeable;

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
public record ThreadPool(String name, ExecutorService executor) implements Closeable, Identifiable {

    public ThreadPool(String name, ExecutorService executor) {
        this.name = AssertUtil.notBlank(name, "name");
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
    public String id() {
        return name;
    }

    @Override
    public boolean isActive() {
        return !executor.isShutdown();
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
