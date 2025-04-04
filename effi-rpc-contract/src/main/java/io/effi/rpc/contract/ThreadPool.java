package io.effi.rpc.contract;

import io.effi.rpc.common.util.resoruce.Closeable;
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

    public ThreadPool(String name, ExecutorService executor) {
        this.name = AssertUtil.notBlank(name, "name");
        this.executor = AssertUtil.notNull(executor, "executor");
    }

    public String name() {
        return name;
    }

    @Override
    public void close() {
        executor.close();
    }

    @Override
    public boolean isActive() {
        return !executor.isShutdown();
    }

    @Override
    public String managerKey() {
        return name;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executor.execute(command);
    }
}
