package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents a counter value at one point in time.
 */
public record CounterSample(MetricKey key, long value, long timestampNanos) implements MetricSample {

    public CounterSample {
        key = AssertUtil.notNull(key, "key");
    }

    @Override
    public MetricKind kind() {
        return MetricKind.COUNTER;
    }

    @Override
    public MetricUnit unit() {
        return MetricUnit.NONE;
    }
}
