package io.effi.rpc.metrics.filter;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.filter.FilterType;
import io.effi.rpc.base.filter.InvokeFilter;
import io.effi.rpc.metrics.MetricsSupport;

/**
 * Callee Metrics Filter.
 */
public class CalleeExecuteRecordFilter implements InvokeFilter<Envelope.Request, Callee<?>> {

    @Override
    public Result doFilter(InvocationContext<Envelope.Request, Callee<?>> context) {
        MetricsSupport.recordStartTime(context);
        Result result = context.execute();
        MetricsSupport.recordEndTime(context);
        return result;
    }

    @Override
    public FilterType<Envelope.Request, Callee<?>> type() {
        return FilterType.of(Envelope.Request.class, Callee.class);
    }
}
