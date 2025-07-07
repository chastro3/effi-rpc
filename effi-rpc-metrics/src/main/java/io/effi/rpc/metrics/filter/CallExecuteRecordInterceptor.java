package io.effi.rpc.metrics.filter;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.UnitType;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.metrics.MetricsSupport;

import static io.effi.rpc.metrics.filter.CallExecuteRecordInterceptor.NAME;

/**
 * Callee Metrics Filter.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class CallExecuteRecordInterceptor implements CallInterceptor<Message.Request, Callee> {

    public static final String NAME = "callExecuteRecord";

    @Override
    public Result intercept(CallContext<Message.Request, Callee> context, InterceptorChain chain) {
        MetricsSupport.recordStartTime(context);
        Result result = chain.proceed(context);
        MetricsSupport.recordEndTime(context);
        return result;
    }

    @Override
    public UnitType<Message.Request, Callee> unitType() {
        return UnitType.of(Message.Request.class, Callee.class);
    }

}
