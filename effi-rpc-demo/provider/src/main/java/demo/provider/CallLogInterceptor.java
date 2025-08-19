package demo.provider;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.protocol.http.h2.Http2Callee;
import io.effi.rpc.protocol.http.support.HttpRequest;

public class CallLogInterceptor implements Interceptor.CallUnit<HttpRequest, Http2Callee>{

    public void ppHlleo() {
        System.out.println("cccc");
    }

    @Override
    public Interaction.Result intercept(CallContext<HttpRequest, Http2Callee> context, Interceptor.Chain chain) {
        System.out.println("请求地址========>> " + context.message().url());
        return chain.proceed(context);
    }

    @Override
    public UnitType<HttpRequest, Http2Callee> unitType() {
        return UnitType.of(HttpRequest.class, Http2Callee.class);
    }
}
