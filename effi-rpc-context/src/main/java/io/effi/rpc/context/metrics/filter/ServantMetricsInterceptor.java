package io.effi.rpc.context.metrics.filter;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.context.metrics.ServantMetrics;

import static io.effi.rpc.context.metrics.filter.ServantMetricsInterceptor.NAME;

/**
 * Records servant-side request metrics.
 */
@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class ServantMetricsInterceptor implements Interceptor.CallUnit<Request, Servant> {

    public static final String NAME = "servantMetrics";

    @Override
    public Interaction.Result intercept(CallContext<Request, Servant> context, Chain chain) {
        ServantMetrics metrics = ServantMetrics.of(context.peer());
        metrics.beginRequest(context);
        Interaction.Result result = chain.proceed(context);
        metrics.recordRequest(context, result.succeeded());
        return result;
    }

    @Override
    public UnitType<Request, Servant> unitType() {
        return UnitType.cached(Request.class, Servant.class);
    }
}
