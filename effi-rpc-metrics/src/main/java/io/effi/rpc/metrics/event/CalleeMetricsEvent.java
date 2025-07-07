package io.effi.rpc.metrics.event;

import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.metrics.CalleeMetrics;

/**
 * Callee Metrics Event.
 */
public class CalleeMetricsEvent extends MetricsEvent<CalleeMetrics> {

    public CalleeMetricsEvent(CalleeMetrics source, CallContext<?, ?> context, boolean hasException) {
        super(source, context, hasException);
    }
}
