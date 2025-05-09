package io.effi.rpc.util;

import io.effi.rpc.constant.Constant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link ScheduledExecutorService} implementation of {@link Scheduler}.
 */
public class ScheduledThreadPool implements Scheduler {
    private final ScheduledExecutorService executorService;

    public ScheduledThreadPool() {
        this.executorService = Executors.newScheduledThreadPool(Constant.DEFAULT_CPU_THREADS);
    }

    @Override
    public void addDisposable(Runnable runnable, long delay, TimeUnit unit) {
        executorService.schedule(runnable, delay, unit);
    }

    @Override
    public void addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit) {
        executorService.scheduleAtFixedRate(runnable, delay, interval, unit);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    @Override
    public boolean isActive() {
        return !executorService.isShutdown();
    }
}