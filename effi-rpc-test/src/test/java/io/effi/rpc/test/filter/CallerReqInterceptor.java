package io.effi.rpc.test.filter;

import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.protocol.http.h2.Http2Callee;

public class CallerReqInterceptor implements CallInterceptor<Message.Request, Http2Callee> {

    @Override
    public Result intercept(CallContext<Message.Request, Http2Callee> context, InterceptorChain chain) {
        return chain.proceed(context);
    }
}
