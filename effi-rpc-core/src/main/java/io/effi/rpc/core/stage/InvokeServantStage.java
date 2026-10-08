package io.effi.rpc.core.stage;


import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.Stage;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.core.stage.InvokeServantStage.NAME;

/**
 * Handles servant invocation for a received request.
 */
@Extension(NAME)
public class InvokeServantStage implements Stage.CallUnit<Request, Servant> {

    public static final String NAME = "invokeServantStage";

    @Override
    public Interaction.Result process(CallContext<Request, Servant> context, Chain chain) {
        SmartURL url = context.message().url();
        Servant servant = context.peer();
        try {
            return Interaction.Result.success(url, servant.invoke(context.args()));
        } catch (EffiRpcException e) {
            return Interaction.Result.failure(url, e);
        }
    }
}
