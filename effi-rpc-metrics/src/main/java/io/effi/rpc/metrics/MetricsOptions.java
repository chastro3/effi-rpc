package io.effi.rpc.metrics;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.BOOLEAN;
import static io.effi.rpc.option.OptionTypes.LONG;

/**
 * Defines platform-level metrics options.
 */
public interface MetricsOptions {

    /**
     * Specifies whether metric recording and reporting are enabled.
     */
    OptionName<Boolean> ENABLED = BOOLEAN.onlyCurrent("metrics.enabled", true);

    /**
     * Specifies the interval between metric reports in milliseconds.
     */
    OptionName<Long> REPORT_INTERVAL_MILLIS = LONG.onlyCurrent("metrics.reportIntervalMillis", 30_000L);
}
