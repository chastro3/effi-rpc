package io.effi.rpc.metrics;

/**
 * Exposes a current numeric value.
 */
public interface MetricGauge {

    /**
     * Shared no-op gauge used when metrics are disabled or not yet registered.
     */
    MetricGauge NOOP = Noop.INSTANCE;

    double value();

    enum Noop implements MetricGauge {

        INSTANCE;

        @Override
        public double value() {
            return 0D;
        }
    }
}
