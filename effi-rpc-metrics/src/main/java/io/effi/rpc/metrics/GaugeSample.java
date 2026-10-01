package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents a gauge value at one point in time.
 */
public record GaugeSample(MetricKey key, double value, long timestampNanos) implements MetricSample {

    public GaugeSample {
        key = AssertUtil.notNull(key, "key");
    }

    @Override
    public MetricKind kind() {
        return MetricKind.GAUGE;
    }

    @Override
    public MetricUnit unit() {
        return MetricUnit.NONE;
    }
}
