package io.effi.rpc.protocol.http.filter;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.protocol.http.filter.HttpRequestInterceptor.NAME;

@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class HttpRequestInterceptor implements Interceptor.CallUnit<HttpRequest, Caller<?>> {

    public static final String NAME = "httpRequestInterceptor";

    @Override
    public Interaction.Result intercept(CallContext<HttpRequest, Caller<?>> context, Chain chain) {
        HttpRequest request = context.message();
        Caller<?> caller = context.peer();
        HttpHeaders headers = request.headers();
        String remoteApplication = caller.option(Caller.REMOTE_APPLICATION);
        if (StringUtil.isNotBlank(remoteApplication)) {
            headers.add(KeyConstant.REQUEST_REMOTE_APPLICATION, remoteApplication);
        }
        String remoteModule = caller.option(Caller.REMOTE_MODULE);
        if (StringUtil.isNotBlank(remoteModule)) {
            headers.add(KeyConstant.REQUEST_REMOTE_MODULE, remoteModule);
        }
        return chain.proceed(context);
    }

    @Override
    public UnitType<HttpRequest, Caller<?>> unitType() {
        return UnitType.cached(HttpRequest.class, Caller.class);
    }
}
