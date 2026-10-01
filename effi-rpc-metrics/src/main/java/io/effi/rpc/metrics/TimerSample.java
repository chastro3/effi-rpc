package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents an aggregated timer snapshot.
 */
public record TimerSample(
        /** Metric identity. */
        MetricKey key,
        /** Number of recorded durations. */
        long count,
        /** Sum of all recorded durations in nanoseconds. */
        long totalNanos,
        /** Longest recorded duration in nanoseconds. */
        long maxNanos,
        /** Shortest recorded duration in nanoseconds. */
        long minNanos,
        /** Sample timestamp in nanoseconds. */
        long timestampNanos
) implements MetricSample {

    public TimerSample {
        AssertUtil.notNull(key, "key");
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

    /**
     * Returns the average recorded duration in nanoseconds, or {@code 0} when nothing was recorded.
     */
    public double averageNanos() {
        return count == 0L ? 0D : (double) totalNanos / count;
    }
}
