package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents an aggregated timer snapshot.
 */
public record TimerSample(
        MetricKey key,
        long count,
        long totalNanos,
        long maxNanos,
        long minNanos,
        long timestampNanos
) implements MetricSample {

    public TimerSample {
        key = AssertUtil.notNull(key, "key");
        AssertUtil.valid(count >= 0L, "count must be >= 0");
        AssertUtil.valid(totalNanos >= 0L, "totalNanos must be >= 0");
        AssertUtil.valid(maxNanos >= 0L, "maxNanos must be >= 0");
        AssertUtil.valid(minNanos >= 0L, "minNanos must be >= 0");
    }

    @Override
    public MetricKind kind() {
        return MetricKind.TIMER;
    }

    @Override
    public MetricUnit unit() {
        return MetricUnit.NANOSECONDS;
    }
}
