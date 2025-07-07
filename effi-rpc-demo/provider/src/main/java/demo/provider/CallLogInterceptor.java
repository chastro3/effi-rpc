package demo.provider;

import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.UnitType;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.support.HttpRequest;

public class CallLogInterceptor implements CallInterceptor<HttpRequest<Object>, Http2Callee> {

    public void ppHlleo() {
        System.out.println("cccc");
    }

    @Override
    public Result intercept(CallContext<HttpRequest<Object>, Http2Callee> context, InterceptorChain chain) {
        System.out.println("请求地址========>> " + context.message().url());
        return chain.proceed(context);
    }

    @Override
    public UnitType<HttpRequest<Object>, Http2Callee> unitType() {
        return UnitType.of(HttpRequest.class, Http2Callee.class);
    }
}
