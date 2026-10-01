package io.effi.rpc.metrics.report;

import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.metrics.MetricsReporter;
import io.effi.rpc.metrics.MetricsSnapshot;

/**
 * Writes a compact metrics snapshot summary to the framework logger.
 */
public final class LoggingMetricsReporter implements MetricsReporter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMetricsReporter.class);

    @Override
    public void report(MetricsSnapshot snapshot) {
        logger.debug("Metrics snapshot: samples={}, timestampNanos={}", snapshot.samples().size(), snapshot.timestampNanos());
    }
}
