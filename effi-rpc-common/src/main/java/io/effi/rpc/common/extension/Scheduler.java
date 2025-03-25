package io.effi.rpc.common.extension;

import io.effi.rpc.common.extension.resoruce.Closeable;

import java.util.concurrent.TimeUnit;

/**
 * Used for publishing disposable or periodic tasks.
 */
public interface Scheduler extends Closeable {

    /**
     * Publish a disposable task.
     *
     * @param runnable the task to run
     * @param delay    the initial delay before the task is executed
     * @param unit     the time unit for the delay
     */
    void addDisposable(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Publish a periodic task.
     *
     * @param runnable the task to run
     * @param delay    the initial delay before the task is executed
     * @param interval the interval between successive executions
     * @param unit     the time unit for both delay and interval
     */
    void addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit);
}

