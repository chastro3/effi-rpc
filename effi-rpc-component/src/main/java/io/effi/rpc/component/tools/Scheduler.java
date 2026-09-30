package io.effi.rpc.component.tools;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.executor.ConfigurableThreadFactory;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.trait.Closeable;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages disposable or periodic tasks.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public class Scheduler implements Closeable {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5L;

    private ScheduledExecutorService disposableService;

    private ScheduledExecutorService periodicService;

    private final LazySingleton<ScheduledExecutorService> defaultService =
            LazySingleton.from(Scheduler::createScheduler);

    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Schedules a disposable task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param unit the time unit for the delay
     * @return the scheduled task
     */
    public ScheduledFuture<?> addDisposable(Runnable runnable, long delay, TimeUnit unit) {
        ensureOpen();
        return disposableService().schedule(runnable, delay, unit);
    }

    /**
     * Schedules a periodic task.
     *
     * @param runnable the task to run
     * @param delay the initial delay before execution
     * @param interval the interval between executions
     * @param unit the time unit for delay and interval
     */
    public ScheduledFuture<?> addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit) {
        ensureOpen();
        return periodicService().scheduleAtFixedRate(runnable, delay, interval, unit);
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
        ensureOpen();
        return disposableService != null ? disposableService : defaultService.ensure();
    }

    public ScheduledExecutorService periodicService() {
        ensureOpen();
        return periodicService != null ? periodicService : defaultService.ensure();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        shutdown(disposableService);
        shutdown(periodicService);
        if (defaultService.initialized()) {
            shutdown(defaultService.ensure());
        }
    }

    @Override
    public boolean active() {
        return !closed.get();
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new RejectedExecutionException("Scheduler is closed");
        }
    }

    private static void shutdown(ScheduledExecutorService executor) {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    private static ScheduledExecutorService createScheduler() {
        ThreadFactory threadFactory = new ConfigurableThreadFactory()
                .namePrefix("rpc-periodic-scheduler")
                .daemon(true);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2, threadFactory);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }
}


