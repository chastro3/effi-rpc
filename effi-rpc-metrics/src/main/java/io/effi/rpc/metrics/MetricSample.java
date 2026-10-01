package io.effi.rpc.metrics;

/**
 * Represents one immutable metric sample.
 */
public sealed interface MetricSample permits CounterSample, GaugeSample, TimerSample {

    /**
     * Returns the identity of the recorded metric.
     */
    MetricKey key();

    /**
     * Returns the kind of the recorded metric.
     */
    MetricKind kind();

    /**
     * Returns the unit of the recorded value.
     */
    MetricUnit unit();

    /**
     * Returns the sample timestamp in nanoseconds.
     */
    long timestampNanos();
}
