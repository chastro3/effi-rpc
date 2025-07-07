package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallStage;
import io.effi.rpc.base.context.StageChain;

import static io.effi.rpc.boot.stage.CallInterceptStage.NAME;

/**
 * Executes the call interceptor chain during the call phase.
 *
 * @see io.effi.rpc.base.context.CallInterceptor
 */
@Extension(NAME)
public class CallInterceptStage implements CallStage<Message.Request, CallSide> {

    public static final String NAME = "callInterceptStage";

    @Override
    public Result execute(CallContext<Message.Request, CallSide> context, StageChain chain) {
        return context.callSide()
                .callInterceptorChain()
                .proceed(context);
    }
}
