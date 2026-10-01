package io.effi.rpc.metrics;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.trait.Closeable;

import java.util.function.DoubleSupplier;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Platform-facing metrics facade.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface Metrics extends Closeable {

    MetricCounter counter(MetricKey key);

    MetricTimer timer(MetricKey key);

    MetricGauge gauge(MetricKey key, DoubleSupplier supplier);

    void register(MetricsRegistrar registrar);

    void registerReporter(MetricsReporter reporter);

    MetricsSnapshot snapshot();

    void report();
}
