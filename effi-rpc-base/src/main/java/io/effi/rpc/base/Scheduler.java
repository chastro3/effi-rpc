package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.concurrent.TimeUnit;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages disposable or periodic tasks.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface Scheduler extends Closeable, EffiRpcPlatform.Provider {

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


