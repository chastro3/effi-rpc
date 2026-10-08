package io.effi.rpc.metrics;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.trait.Closeable;

import java.util.function.DoubleSupplier;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Provides the platform metrics facade.
 * <p>
 * Instruments resolved while metrics are disabled are shared no-ops, so recording never
 * fails the caller path and callers never need to check for a missing registry.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface Metrics extends Closeable {

    /**
     * Returns the counter registered for the supplied key, creating it on first use.
     *
     * @param key metric identity
     * @return counter bound to the key
     */
    MetricCounter counter(MetricKey key);

    /**
     * Returns the timer registered for the supplied key, creating it on first use.
     *
     * @param key metric identity
     * @return timer bound to the key
     */
    MetricTimer timer(MetricKey key);

    /**
     * Returns the gauge registered for the supplied key, creating it on first use.
     *
     * @param key      metric identity
     * @param supplier current value supplier
     * @return gauge bound to the key
     */
    MetricGauge gauge(MetricKey key, DoubleSupplier supplier);

    /**
     * Registers every instrument owned by the supplied registrar.
     *
     * @param registrar component-owned registrar
     */
    void register(MetricsRegistrar registrar);

    /**
     * Registers a reporter receiving each published snapshot.
     *
     * @param reporter snapshot reporter
     */
    void registerReporter(MetricsReporter reporter);

    /**
     * Returns the current snapshot of every registered instrument.
     */
    MetricsSnapshot snapshot();

    /**
     * Publishes the current snapshot to every registered reporter.
     */
    void report();
}
