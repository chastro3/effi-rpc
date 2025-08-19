package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.boot.confiurator.stage.ChosenInterceptStage.NAME;

/**
 * Executes the chosen interceptor chain during the call phase.
 *
 * @see io.effi.rpc.context.context.CallInterceptor
 */
@Extension(NAME)
public class ChosenInterceptStage implements Stage.CallUnit<Request, Caller<?>> {

    public static final String NAME = "chosenInterceptStage";

    @Override
    public Interaction.Result process(CallContext<Request, Caller<?>> context, Chain chain) {
        return context.peer()
                .chosenInterceptorChain()
                .proceed(context);
    }
}
