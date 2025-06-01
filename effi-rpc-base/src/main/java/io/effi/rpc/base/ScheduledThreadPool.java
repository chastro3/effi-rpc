package io.effi.rpc.base;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.PlatformSource;
import io.effi.rpc.constant.Constant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implements {@link Scheduler} using {@link ScheduledExecutorService}.
 */
public class ScheduledThreadPool extends PlatformSource.Holder implements Scheduler {

    private final ScheduledExecutorService executorService;

    public ScheduledThreadPool(EffiRpcPlatform platform) {
        super(platform);
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