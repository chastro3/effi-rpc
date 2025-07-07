package io.effi.rpc.metrics.filter;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.context.ReplyInterceptor;
import io.effi.rpc.base.context.UnitType;
import io.effi.rpc.base.event.EventDispatcher;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.metrics.event.CallerMetricsEvent;

import static io.effi.rpc.metrics.filter.CallerMetricsInterceptor.NAME;

/**
 * Caller Metrics Filter.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class CallerMetricsInterceptor implements ReplyInterceptor<Message.Response, Caller<?>> {

    public static final String NAME = "callerMetrics";

    @Override
    public Result intercept(ReplyContext<Message.Response, Caller<?>> context, InterceptorChain chain) {
        CallContext<?, Caller<?>> callContext = context.callContext();
        MetricsSupport.recordEndTime(callContext);
        Caller<?> callee = context.callSide();
        CallerMetrics callerMetrics = callee.get(CallerMetrics.GENERIC_KEY);
        Result result = chain.proceed(context);
        context.platform().lookup(EventDispatcher.class)
                .publish(new CallerMetricsEvent(callerMetrics, callContext, result.hasException()));
        return result;
    }


    @Override
    public UnitType<Message.Response, Caller<?>> unitType() {
        return UnitType.of(Message.Response.class, Caller.class);
    }
}
