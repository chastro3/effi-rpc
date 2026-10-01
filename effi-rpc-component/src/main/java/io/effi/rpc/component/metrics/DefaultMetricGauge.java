package io.effi.rpc.component.metrics;

import io.effi.rpc.metrics.MetricGauge;
import io.effi.rpc.util.AssertUtil;

import java.util.function.DoubleSupplier;

/**
 * Default gauge backed by a value supplier.
 */
final class DefaultMetricGauge implements MetricGauge {

    private final DoubleSupplier supplier;

    DefaultMetricGauge(DoubleSupplier supplier) {
        this.supplier = AssertUtil.notNull(supplier, "supplier");
    }

    @Override
    public double value() {
        return supplier.getAsDouble();
    }
}
