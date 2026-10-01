package io.effi.rpc.metrics;

/**
 * Defines how a component hands its owned instruments to the platform metrics center.
 */
public interface MetricsRegistrar {

    /**
     * Registers metrics for the implementation's owning component.
     *
     * @param metrics platform metrics center
     */
    void register(Metrics metrics);
}
