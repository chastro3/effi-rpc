package io.effi.rpc.metrics.filter;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.event.EventDispatcher;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.ReplyFilter;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.metrics.event.CallerMetricsEvent;

/**
 * Caller Metrics Filter.
 */
public class CallerMetricsFilter implements ReplyFilter<Envelope.Response, Caller<?>> {


    @Override
    public Result doFilter(ReplyContext<Envelope.Response, Caller<?>> context) {
        InvocationContext<?, Caller<?>> invocationContext = context.invocationContext();
        MetricsSupport.recordEndTime(invocationContext);
        Caller<?> callee = context.invoker();
        CallerMetrics callerMetrics = callee.get(CallerMetrics.GENERIC_KEY);
        Result result = context.execute();
        context.platform().lookup(EventDispatcher.class)
                .publish(new CallerMetricsEvent(callerMetrics, invocationContext, result.hasException()));
        return result;
    }

    @Override
    public FilterType<Envelope.Response, Caller<?>> type() {
        return FilterType.of(Envelope.Response.class, Caller.class);
    }
}
