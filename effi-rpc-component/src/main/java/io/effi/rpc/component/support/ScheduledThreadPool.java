package io.effi.rpc.component.support;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implements {@link Scheduler} using {@link ScheduledExecutorService}.
 */
public class ScheduledThreadPool extends ScopedPlatform.Holder  {

    private final ScheduledExecutorService executorService;

    public ScheduledThreadPool(ScopedPlatform platform) {
        super(platform);
        this.executorService = Executors.newScheduledThreadPool(Constant.DEFAULT_CPU_THREADS);
    }

    public void addDisposable(Runnable runnable, long delay, TimeUnit unit) {
        executorService.schedule(runnable, delay, unit);
    }

    public void addPeriodic(Runnable runnable, long delay, long interval, TimeUnit unit) {
        executorService.scheduleAtFixedRate(runnable, delay, interval, unit);
    }

    public void close() {
        executorService.shutdown();
    }

    public boolean isActive() {
        return !executorService.isShutdown();
    }
}