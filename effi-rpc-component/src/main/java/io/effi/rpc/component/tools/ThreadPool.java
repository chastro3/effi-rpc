package io.effi.rpc.component.tools;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.ConcurrentErrorCodes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.trait.Closeable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    /**
     * Executes a result-producing task.
     *
     * @param supplier task supplier
     * @param <T> result type
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
            promise.failure(ConcurrentErrorCodes.TASK_REJECTED.fail(e, "submit"));
        }
        return promise;
    }

    /**
     * Returns the current thread pool metrics snapshot.
     */
    public Metrics metrics() {
        if (executor instanceof ThreadPoolExecutor pool) {
            return new Metrics(pool.getActiveCount(), pool.getQueue().size(), pool.getCompletedTaskCount());
        }
        return new Metrics(-1, -1, -1);
    }

    @Override
    public boolean active() {
        return !executor.isShutdown();
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

    /**
     * Captures thread pool metrics.
     */
    public record Metrics(
            /** Number of active tasks. */
            int activeCount,
            /** Number of queued tasks. */
            int queueSize,
            /** Number of completed tasks. */
            long completedTaskCount
    ) {
    }
}
