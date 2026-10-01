package io.effi.rpc.metrics;

import io.effi.rpc.util.AssertUtil;

import java.util.List;

/**
 * Represents a point-in-time metrics snapshot.
 */
public record MetricsSnapshot(long timestampNanos, List<MetricSample> samples) {

    public MetricsSnapshot {
        AssertUtil.valid(timestampNanos >= 0L, "timestampNanos must be >= 0");
        samples = samples == null ? List.of() : List.copyOf(samples);
    }

    public static MetricsSnapshot empty() {
        return new MetricsSnapshot(System.nanoTime(), List.of());
    }
}
