package io.effi.rpc.test.filter;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.protocol.http.h2.Http2Servant;

public class CallerReqInterceptor implements Interceptor.CallUnit<Request, Http2Servant> {

    @Override
    public Interaction.Result intercept(CallContext<Request, Http2Servant> context, Chain chain) {
        return chain.proceed(context);
    }
}
