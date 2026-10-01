package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents a gauge value at one point in time.
 */
public record GaugeSample(
        /** Metric identity. */
        MetricKey key,
        /** Gauge value at the sample timestamp. */
        double value,
        /** Sample timestamp in nanoseconds. */
        long timestampNanos
) implements MetricSample {

    public GaugeSample {
        AssertUtil.notNull(key, "key");
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
