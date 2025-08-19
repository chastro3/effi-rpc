package io.effi.rpc.context.metrics.filter;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.context.metrics.MetricsSupport;

import static io.effi.rpc.context.metrics.filter.CallExecuteRecordInterceptor.NAME;

/**
 * Callee Metrics Filter.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class CallExecuteRecordInterceptor implements Interceptor.CallUnit<Request, Callee> {

    public static final String NAME = "callExecuteRecord";

    @Override
    public Interaction.Result intercept(CallContext<Request, Callee> context, Chain chain) {
        MetricsSupport.recordStartTime(context);
        Interaction.Result result = chain.proceed(context);
        MetricsSupport.recordEndTime(context);
        return result;
    }

    @Override
    public UnitType<Request, Callee> unitType() {
        return UnitType.of(Request.class, Callee.class);
    }

}
