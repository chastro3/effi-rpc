package io.effi.rpc.component.metrics;

import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.TimerSample;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Default timer backed by atomic aggregate counters.
 */
final class DefaultMetricTimer implements MetricTimer {

    private final MetricKey key;

    private final LongAdder count = new LongAdder();

    private final LongAdder totalNanos = new LongAdder();

    private final AtomicLong maxNanos = new AtomicLong();

    private final AtomicLong minNanos = new AtomicLong(Long.MAX_VALUE);

    DefaultMetricTimer(MetricKey key) {
        this.key = key;
    }

    @Override
    public void recordNanos(long durationNanos) {
        if (durationNanos < 0L) {
            throw new IllegalArgumentException("durationNanos must be >= 0");
        }
        count.increment();
        totalNanos.add(durationNanos);
        maxNanos.accumulateAndGet(durationNanos, Math::max);
        minNanos.accumulateAndGet(durationNanos, Math::min);
    }

    @Override
    public TimerSample snapshot() {
        long currentCount = count.sum();
        long currentMin = currentCount == 0L ? 0L : minNanos.get();
        return new TimerSample(
                key,
                currentCount,
                totalNanos.sum(),
                maxNanos.get(),
                currentMin,
                System.nanoTime()
        );
    }
}
