package io.effi.rpc.component.tools;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.concurrent.ConcurrentErrorCodes;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.util.AssertUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages a named thread pool bound to a platform.
 */
@ScopedComponent(scope = PLATFORM)
public final class ThreadPool implements Closeable, Identifiable {

    private final String id;

    private final ExecutorService executor;

    private final ThreadPoolMetrics metrics;

    public ThreadPool(String id, ExecutorService executor) {
        this.id = AssertUtil.notBlank(id, "id");
        this.executor = AssertUtil.notNull(executor, "executor");
        this.metrics = new ThreadPoolMetrics(this);
    }

    /**
     * Returns the underlying executor.
     */
    public ExecutorService executor() {
        return executor;
    }

    /**
     * Returns the metrics owned by this thread pool.
     */
    public ThreadPoolMetrics metrics() {
        return metrics;
    }

    /**
     * Executes a result-producing task.
     *
     * @param supplier task supplier
     * @param <T>      result type
     * @return task future
     */
    public <T> Future<T> execute(Supplier<T> supplier) {
        Promise<T> promise = new Promise<>();
        try {
            executor.submit(() -> {
                try {
                    T result = supplier.get();
                    promise.success(result);
                } catch (Throwable e) {
                    promise.failure(ConcurrentErrorCodes.TASK_FAILED.fail(e, "submit"));
                }
            });
        } catch (RejectedExecutionException e) {
            metrics.rejected();
            promise.failure(ConcurrentErrorCodes.TASK_REJECTED.fail(e, "submit"));
        }
        return promise;
    }

    /**
     * Executes a side-effecting task.
     *
     * @param task task to execute
     * @return task future
     */
    public Future<Void> execute(Runnable task) {
        Promise<Void> promise = new Promise<>();
        try {
            executor.submit(() -> {
                try {
                    task.run();
                    promise.success(null);
                } catch (Throwable e) {
                    promise.failure(ConcurrentErrorCodes.TASK_FAILED.fail(e, "submit"));
                }
            });
        } catch (RejectedExecutionException e) {
            metrics.rejected();
            promise.failure(ConcurrentErrorCodes.TASK_REJECTED.fail(e, "submit"));
        }
        return promise;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    @Override
    public boolean active() {
        return !executor.isShutdown();
    }
}
