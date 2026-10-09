package io.effi.rpc.component.tools;

import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Registers and records metrics for one named thread pool.
 */
public final class ThreadPoolMetrics implements MetricsRegistrar {

    public static final MetricKey ACTIVE = MetricKey.of("rpc.thread.pool.active");

    public static final MetricKey QUEUE_SIZE = MetricKey.of("rpc.thread.pool.queue.size");

    public static final MetricKey REJECTED_COUNT = MetricKey.of("rpc.thread.pool.rejected.count");

    private final ThreadPool threadPool;

    private MetricCounter rejected = MetricCounter.NOOP;

    public ThreadPoolMetrics(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    @Override
    public void register(Metrics metrics) {
        String pool = threadPool.id();
        this.rejected = metrics.counter(REJECTED_COUNT.withTag("pool", pool));
        metrics.gauge(ACTIVE.withTag("pool", pool), this::activeCount);
        metrics.gauge(QUEUE_SIZE.withTag("pool", pool), this::queueSize);
    }

    void rejected() {
        rejected.increment();
    }

    private double activeCount() {
        return threadPool.executor() instanceof ThreadPoolExecutor executor ? executor.getActiveCount() : 0D;
    }

    private double queueSize() {
        return threadPool.executor() instanceof ThreadPoolExecutor executor ? executor.getQueue().size() : 0D;
    }
}
