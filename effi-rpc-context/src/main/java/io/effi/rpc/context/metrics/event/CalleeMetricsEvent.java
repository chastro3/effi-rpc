package io.effi.rpc.context.metrics.event;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.metrics.CalleeMetrics;

/**
 * Callee Metrics Event.
 */
public class CalleeMetricsEvent extends MetricsEvent<CalleeMetrics> {

    public CalleeMetricsEvent(CalleeMetrics source, CallContext<?, ?> context, boolean succeeded) {
        super(source, context, succeeded);
    }
}
