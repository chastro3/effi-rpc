package io.effi.rpc.metrics.event;

import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.metrics.CallerMetrics;

/**
 * Callee Metrics Event.
 */
public class CallerMetricsEvent extends MetricsEvent<CallerMetrics> {

    public CallerMetricsEvent(CallerMetrics source, InvocationContext<?, ?> context, boolean hasException) {
        super(source, context, hasException);
    }
}
