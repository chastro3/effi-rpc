package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Represents a named thread pool that wraps an {@link ExecutorService},
 * providing lifecycle management and execution capabilities.
 */
@ScopedComponent(scope = PLATFORM)
public class ThreadPool extends EffiRpcPlatform.Holder implements Closeable, Identifiable {

    private final String name;

    private final ExecutorService executor;

    public ThreadPool(String name, ExecutorService executor) {
        this(name, executor, null);
    }

    public ThreadPool(String name, ExecutorService executor, EffiRpcPlatform platform) {
        super(platform);
        this.name = AssertUtil.notBlank(name, "name");
        this.executor = AssertUtil.notNull(executor, "executor");
    }

    public String name() {
        return name;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    @Override
    public boolean isActive() {
        return !executor.isShutdown();
    }

    @Override
    public String id() {
        return name;
    }

    public CompletableFuture<Void> execute(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    public ExecutorService executor() {
        return executor;
    }
}
