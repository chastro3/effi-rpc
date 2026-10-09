package io.effi.rpc.component.tools;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.metrics.MetricKey;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadPoolMetricsTest {

    @Test
    void registersPoolMetricsWithPoolTag() {
        ScopedPlatform platform = new ScopedPlatform("thread-pool-metrics");
        DefaultMetrics metrics = new DefaultMetrics(platform);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(4)
        );
        ThreadPool threadPool = new ThreadPool("caller-hybrid", executor);

        metrics.register(threadPool.metrics());

        assertEquals(0D, gauge(metrics, ThreadPoolMetrics.ACTIVE, "caller-hybrid"));
        assertEquals(0D, gauge(metrics, ThreadPoolMetrics.QUEUE_SIZE, "caller-hybrid"));

        executor.shutdownNow();
        metrics.close();
        platform.close();
    }

    @Test
    void countsRejectedTasks() {
        ScopedPlatform platform = new ScopedPlatform("thread-pool-rejected");
        DefaultMetrics metrics = new DefaultMetrics(platform);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1)
        );
        ThreadPool threadPool = new ThreadPool("servant-hybrid", executor);
        metrics.register(threadPool.metrics());
        executor.shutdown();

        threadPool.execute(() -> {
        });

        assertEquals(1L, metrics.counter(
                ThreadPoolMetrics.REJECTED_COUNT.withTag("pool", "servant-hybrid")
        ).count());

        metrics.close();
        platform.close();
    }

    private static double gauge(DefaultMetrics metrics, MetricKey key, String pool) {
        return metrics.gauge(key.withTag("pool", pool), () -> -1D).value();
    }
}
