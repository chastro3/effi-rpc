package io.effi.rpc.contract;

import io.effi.rpc.common.extension.resoruce.Closeable;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.manager.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * A named thread pool that wraps an {@link ExecutorService}, providing
 * lifecycle management and execution capabilities.
 */
public class ThreadPool implements Closeable, Manager.Key, Executor {

    private final String name;

    private final ExecutorService executor;

    /**
     * Creates a thread pool with the given name and executor service.
     *
     * @param name     the thread pool name; must not be blank
     * @param executor the executor service; must not be null
     */
    public ThreadPool(String name, ExecutorService executor) {
        this.name = AssertUtil.notBlank(name, "name");
        this.executor = AssertUtil.notNull(executor, "executor");
    }

    /**
     * Returns the thread pool name.
     */
    public String name() {
        return name;
    }

    /**
     * Shuts down the thread pool, releasing resources.
     */
    @Override
    public void close() {
        executor.close();
    }

    /**
     * Checks if the thread pool is active.
     *
     * @return {@code true} if not shut down, otherwise {@code false}
     */
    @Override
    public boolean isActive() {
        return !executor.isShutdown();
    }

    /**
     * Returns the thread pool name as its unique key.
     */
    @Override
    public String managerKey() {
        return name;
    }

    /**
     * Executes a task.
     *
     * @param command the task; must not be null
     */
    @Override
    public void execute(@NotNull Runnable command) {
        executor.execute(command);
    }
}
