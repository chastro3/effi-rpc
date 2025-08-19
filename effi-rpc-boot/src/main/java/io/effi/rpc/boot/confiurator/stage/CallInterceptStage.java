package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.boot.confiurator.stage.CallInterceptStage.NAME;

/**
 * Executes the call interceptor chain during the call phase.
 *
 * @see io.effi.rpc.context.context.CallInterceptor
 */
@Extension(NAME)
public class CallInterceptStage implements Stage.CallUnit<Request, Peer> {

    public static final String NAME = "callInterceptStage";

    @Override
    public Interaction.Result process(CallContext<Request, Peer> context, Chain chain) {
        return context.peer()
                .callInterceptorChain()
                .proceed(context);
    }

}
