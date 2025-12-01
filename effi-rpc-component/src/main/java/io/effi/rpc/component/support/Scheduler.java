package io.effi.rpc.component.support;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.executor.ConfigurableThreadFactory;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.trait.Closeable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages disposable or periodic tasks.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public class Scheduler implements Closeable {

    private static final LazySingleton<ScheduledExecutorService> DEFAULT_SCHEDULER =
            LazySingleton.from(Scheduler::initializeDefaultPeriodicService);

    private ScheduledExecutorService disposableService;

    private ScheduledExecutorService periodicService;

    /**
     * Schedules a disposable task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param unit the time unit for the delay
     */
    public void addDisposable(Runnable runnable, long delay, TimeUnit unit) {
        disposableService().schedule(runnable, delay, unit);
    }

    /**
     * Schedules a periodic task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param interval the interval between executions
     * @param unit the time unit for delay and interval
     */
    public void addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit) {
        periodicService().scheduleAtFixedRate(runnable, delay, interval, unit);
    }

    public Scheduler disposableService(ScheduledExecutorService disposableService) {
        this.disposableService = disposableService;
        return this;
    }

    public Scheduler periodicService(ScheduledExecutorService periodicService) {
        this.periodicService = periodicService;
        return this;
    }

    public ScheduledExecutorService disposableService() {
        return disposableService != null ? disposableService : DEFAULT_SCHEDULER.ensure();
    }

    public ScheduledExecutorService periodicService() {
        return periodicService != null ? periodicService : DEFAULT_SCHEDULER.ensure();
    }

    @Override
    public void close() {
        if (active()) {
            if (disposableService != null) {
                disposableService.shutdown();
            }
            if (periodicService != null) {
                periodicService.shutdown();
            }
            if (DEFAULT_SCHEDULER.initialized()) {
                DEFAULT_SCHEDULER.ensure().shutdown();
            }
        }
    }

    @Override
    public boolean active() {
        return !(!DEFAULT_SCHEDULER.initialized()
                && disposableService() == null
                && periodicService() == null);
    }

    private static ScheduledThreadPoolExecutor initializeDefaultPeriodicService() {
        ThreadFactory threadFactory = new ConfigurableThreadFactory()
                .namePrefix("rpc-periodic-scheduler")
                .daemon(true);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }
}


