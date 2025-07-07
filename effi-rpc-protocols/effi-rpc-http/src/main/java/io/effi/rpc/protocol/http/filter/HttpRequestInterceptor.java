package io.effi.rpc.protocol.http.filter;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.UnitType;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.protocol.http.filter.HttpRequestInterceptor.NAME;

@Extension(value = NAME, tags = Tags.FORCE_ACTIVE)
public class HttpRequestInterceptor implements CallInterceptor<HttpRequest<Object>, Caller<?>> {

    public static final String NAME = "httpRequestInterceptor";

    @Override
    public Result intercept(CallContext<HttpRequest<Object>, Caller<?>> context, InterceptorChain chain) {
        HttpRequest<Object> request = context.message();
        Caller<?> caller = context.callSide();
        HttpHeaders headers = request.headers();
        String remoteApplication = caller.getConfig(DefaultConfigNames.REMOTE_APPLICATION);
        if (StringUtil.isNotBlank(remoteApplication)) {
            headers.add(KeyConstant.REQUEST_REMOTE_APPLICATION, remoteApplication);
        }
        String remoteModule = caller.getConfig(DefaultConfigNames.REMOTE_MODULE);
        if (StringUtil.isNotBlank(remoteModule)) {
            headers.add(KeyConstant.REQUEST_REMOTE_MODULE, remoteModule);
        }
        return chain.proceed(context);
    }

    @Override
    public UnitType<HttpRequest<Object>, Caller<?>> unitType() {
        return UnitType.of(HttpRequest.class, Caller.class);
    }
}
