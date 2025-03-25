package io.effi.rpc.metrics.filter;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.filter.InvokeFilter;
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
}
