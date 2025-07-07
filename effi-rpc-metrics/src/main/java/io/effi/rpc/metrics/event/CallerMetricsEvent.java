package io.effi.rpc.metrics.event;

import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.metrics.CallerMetrics;

/**
 * Callee Metrics Event.
 */
public class CallerMetricsEvent extends MetricsEvent<CallerMetrics> {

    public CallerMetricsEvent(CallerMetrics source, CallContext<?, ?> context, boolean hasException) {
        super(source, context, hasException);
    }
}
