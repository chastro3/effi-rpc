package io.effi.rpc.metrics;

/**
 * Registers component-owned instruments into the platform metrics center.
 */
public interface MetricsRegistrar {

    /**
     * Registers metrics for the implementation's owning component.
     *
     * @param metrics platform metrics center
     */
    void register(Metrics metrics);
}
