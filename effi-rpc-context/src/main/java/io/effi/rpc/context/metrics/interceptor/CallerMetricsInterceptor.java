package io.effi.rpc.context.metrics.interceptor;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.context.metrics.CallerMetrics;

import static io.effi.rpc.context.metrics.interceptor.CallerMetricsInterceptor.NAME;

/**
 * Records caller-side call metrics.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class CallerMetricsInterceptor implements Interceptor.ReplyUnit<Response, Caller<?>> {

    public static final String NAME = "callerMetrics";

    @Override
    public Interaction.Result intercept(ReplyContext<Response, Caller<?>> context, Chain chain) {
        CallerMetrics metrics = CallerMetrics.of(context.peer());
        Interaction.Result result = chain.proceed(context);
        metrics.recordCall(context.callContext(), result.succeeded());
        return result;
    }

    @Override
    public UnitType<Response, Caller<?>> unitType() {
        return UnitType.cached(Response.class, Caller.class);
    }
}
