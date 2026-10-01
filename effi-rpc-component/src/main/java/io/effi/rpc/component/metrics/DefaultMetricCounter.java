package io.effi.rpc.component.metrics;

import io.effi.rpc.metrics.MetricCounter;

import java.util.concurrent.atomic.LongAdder;

/**
 * Default counter backed by a {@link LongAdder}.
 */
final class DefaultMetricCounter implements MetricCounter {

    private final LongAdder value = new LongAdder();

    @Override
    public void increment() {
        value.increment();
    }

    @Override
    public void add(long amount) {
        if (amount != 0L) {
            value.add(amount);
        }
    }

    @Override
    public long count() {
        return value.sum();
    }
}
