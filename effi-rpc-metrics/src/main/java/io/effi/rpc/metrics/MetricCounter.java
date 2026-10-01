package io.effi.rpc.metrics;

/**
 * Records monotonic counter values.
 */
public interface MetricCounter {

    /**
     * Shared no-op counter used when metrics are disabled or not yet registered.
     */
    MetricCounter NOOP = Noop.INSTANCE;

    void increment();

    void add(long amount);

    long count();

    enum Noop implements MetricCounter {

        INSTANCE;

        @Override
        public void increment() {
        }

        @Override
        public void add(long amount) {
        }

        @Override
        public long count() {
            return 0L;
        }
    }
}
