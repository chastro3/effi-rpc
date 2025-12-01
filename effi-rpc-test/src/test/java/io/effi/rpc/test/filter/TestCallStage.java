package io.effi.rpc.test.filter;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Stage;
import io.effi.rpc.protocol.http.h2.Http2Servant;
import io.effi.rpc.protocol.http.support.HttpRequest;

/**
 * @Author WenBo Zhou
 * @Date 2025/8/9 18:57
 */
public class TestCallStage implements Stage.CallUnit<HttpRequest, Http2Servant> {
    @Override
    public Interaction.Result process(CallContext<HttpRequest, Http2Servant> context, Chain chain) {
        return chain.proceed(context);
    }
}
