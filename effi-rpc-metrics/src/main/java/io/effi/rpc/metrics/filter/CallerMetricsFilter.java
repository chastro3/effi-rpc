package io.effi.rpc.metrics.filter;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.contract.filter.FilterType;
import io.effi.rpc.contract.filter.ReplyFilter;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.repository.FilterRepository;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.metrics.event.CallerMetricsEvent;

/**
 * Caller Metrics Filter.
 */
public class CallerMetricsFilter implements ReplyFilter<Envelope.Response, Caller<?>> {

    public CallerMetricsFilter(EffiRpcModule module) {
        FilterRepository manager = module.filterRepository();
    }

    @Override
    public Result doFilter(ReplyContext<Envelope.Response, Caller<?>> context) {
        InvocationContext<?, Caller<?>> invocationContext = context.invocationContext();
        MetricsSupport.recordEndTime(invocationContext);
        Caller<?> callee = context.invoker();
        CallerMetrics callerMetrics = callee.get(CallerMetrics.GENERIC_KEY);
        Result result = context.execute();
        context.module().application().publishEvent(new CallerMetricsEvent(callerMetrics, invocationContext, result.hasException()));
        return result;
    }

    @Override
    public FilterType<Envelope.Response, Caller<?>> type() {
        return FilterType.of(Envelope.Response.class, Caller.class);
    }
}
