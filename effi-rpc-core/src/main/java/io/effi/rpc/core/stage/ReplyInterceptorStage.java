package io.effi.rpc.core.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.core.stage.ReplyInterceptorStage.NAME;

/**
 * Handles the reply interceptor chain during the reply phase.
 *
 * @see io.effi.rpc.context.Interceptor.ReplyUnit
 */
@Extension(NAME)
public class ReplyInterceptorStage implements Stage.ReplyUnit<Response, Peer> {

    public static final String NAME = "replyInterceptorStage";

    @Override
    public Interaction.Result process(ReplyContext<Response, Peer> context, Chain chain) {
        return context.peer()
                .replyInterceptorChain()
                .proceed(context);
    }
}
