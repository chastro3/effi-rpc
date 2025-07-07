package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallStage;
import io.effi.rpc.base.context.StageChain;

import static io.effi.rpc.boot.stage.ChosenInterceptStage.NAME;

/**
 * Executes the chosen interceptor chain during the call phase.
 *
 * @see io.effi.rpc.base.context.CallInterceptor
 */
@Extension(NAME)
public class ChosenInterceptStage implements CallStage<Message.Request, Caller<?>> {

    public static final String NAME = "chosenInterceptStage";

    @Override
    public Result execute(CallContext<Message.Request, Caller<?>> context, StageChain chain) {
        return context.callSide()
                .chosenInterceptorChain()
                .proceed(context);
    }
}
