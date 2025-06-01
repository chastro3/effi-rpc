package io.effi.rpc.metrics.event;

import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.metrics.CalleeMetrics;

/**
 * Callee Metrics Event.
 */
public class CalleeMetricsEvent extends MetricsEvent<CalleeMetrics> {

    public CalleeMetricsEvent(CalleeMetrics source, InvocationContext<?, ?> context, boolean hasException) {
        super(source, context, hasException);
    }
}
