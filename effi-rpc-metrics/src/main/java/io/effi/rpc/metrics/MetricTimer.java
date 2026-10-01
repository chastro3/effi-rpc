package io.effi.rpc.metrics;

/**
 * Records operation durations and exposes aggregated snapshots.
 */
public interface MetricTimer {

    /**
     * Shared no-op timer used when metrics are disabled or not yet registered.
     */
    MetricTimer NOOP = Noop.INSTANCE;

    /**
     * Records one operation duration.
     *
     * @param durationNanos duration in nanoseconds
     */
    void recordNanos(long durationNanos);

    /**
     * Returns the current aggregated snapshot.
     */
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
