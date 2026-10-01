package io.effi.rpc.metrics;

/**
 * Represents one immutable metric sample.
 */
public sealed interface MetricSample permits CounterSample, GaugeSample, TimerSample {

    MetricKey key();

    MetricKind kind();

    MetricUnit unit();

    long timestampNanos();
}
