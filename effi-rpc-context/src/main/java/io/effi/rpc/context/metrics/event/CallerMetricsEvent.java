package io.effi.rpc.context.metrics.event;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.metrics.CallerMetrics;

/**
 * Callee Metrics Event.
 */
public class CallerMetricsEvent extends MetricsEvent<CallerMetrics> {

    public CallerMetricsEvent(CallerMetrics source, CallContext<?, ?> context, boolean succeeded) {
        super(source, context, succeeded);
    }
}
