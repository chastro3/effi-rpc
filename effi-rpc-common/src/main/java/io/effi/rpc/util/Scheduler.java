package io.effi.rpc.util;

import io.effi.rpc.util.resoruce.Closeable;

import java.util.concurrent.TimeUnit;

/**
 * Manages disposable or periodic tasks.
 */
public interface Scheduler extends Closeable {

    /**
     * Schedules a disposable task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param unit the time unit for the delay
     */
    void addDisposable(Runnable runnable, long delay, TimeUnit unit);

    /**
     * Schedules a periodic task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param interval the interval between executions
     * @param unit the time unit for delay and interval
     */
    void addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit);
}


