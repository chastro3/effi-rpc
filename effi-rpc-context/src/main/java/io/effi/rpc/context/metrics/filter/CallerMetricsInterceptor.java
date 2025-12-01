package io.effi.rpc.context.metrics.filter;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.event.EventDispatcher;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.metrics.MetricsSupport;
import io.effi.rpc.context.metrics.event.CallerMetricsEvent;

import static io.effi.rpc.context.metrics.filter.CallerMetricsInterceptor.NAME;

/**
 * Caller Metrics Filter.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class CallerMetricsInterceptor implements Interceptor.ReplyUnit<Response, Caller<?>> {

    public static final String NAME = "callerMetrics";

    @Override
    public Interaction.Result intercept(ReplyContext<Response, Caller<?>> context, Chain chain) {
        CallContext<?, Caller<?>> callContext = context.callContext();
        MetricsSupport.recordEndTime(callContext);
        Caller<?> callee = context.peer();
        CallerMetrics callerMetrics = callee.get(CallerMetrics.GENERIC_KEY);
        Interaction.Result result = chain.proceed(context);
        context.platform().singleComponent(EventDispatcher.class)
                .publish(new CallerMetricsEvent(callerMetrics, callContext, result.succeeded()));
        return result;
    }

    @Override
    public UnitType<Response, Caller<?>> unitType() {
        return UnitType.cached(Response.class, Caller.class);
    }
}
