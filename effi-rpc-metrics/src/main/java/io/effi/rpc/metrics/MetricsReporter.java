package io.effi.rpc.metrics;

/**
 * Exports one metrics snapshot to an external system.
 */
public interface MetricsReporter {

    /**
     * Reports a point-in-time metrics snapshot.
     *
     * @param snapshot metrics snapshot
     */
    void report(MetricsSnapshot snapshot);
}
