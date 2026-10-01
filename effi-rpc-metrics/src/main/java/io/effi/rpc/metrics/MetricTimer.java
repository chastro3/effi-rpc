package io.effi.rpc.metrics;

/**
 * Records operation durations and exposes aggregated snapshots.
 */
public interface MetricTimer {

    /**
     * Shared no-op timer used when metrics are disabled or not yet registered.
     */
    MetricTimer NOOP = Noop.INSTANCE;

    void recordNanos(long durationNanos);

    TimerSample snapshot();

    enum Noop implements MetricTimer {

        INSTANCE;

        @Override
        public void recordNanos(long durationNanos) {
        }

        @Override
        public TimerSample snapshot() {
            return new TimerSample(MetricKey.of("metrics.noop.timer"), 0L, 0L, 0L, 0L, System.nanoTime());
        }
    }
}
