package io.effi.rpc.metrics;

/**
 * Records monotonic counter values.
 */
public interface MetricCounter {

    /**
     * Shared no-op counter used when metrics are disabled or not yet registered.
     */
    MetricCounter NOOP = Noop.INSTANCE;

    /**
     * Increments the counter by one.
     */
    void increment();

    /**
     * Adds the supplied amount to the counter.
     *
     * @param amount amount to add
     */
    void add(long amount);

    /**
     * Returns the current total count.
     */
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
