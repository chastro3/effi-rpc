package io.effi.rpc.core.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.core.stage.InteractionDispatchStage.NAME;

/**
 * Handles one interaction by delegating to the mode selected by its call context.
 */
@Extension(NAME)
public class InteractionDispatchStage implements Stage.CallUnit<Request, Caller<?>> {

    public static final String NAME = "interactionDispatchStage";

    @Override
    public Interaction.Result process(CallContext<Request, Caller<?>> context, Chain chain) {
        return context.mode().dispatch(context);
    }
}
