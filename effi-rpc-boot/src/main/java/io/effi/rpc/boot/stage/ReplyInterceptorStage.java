package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.boot.stage.ReplyInterceptorStage.NAME;

/**
 * Handles the reply interceptor chain during the reply phase.
 *
 * @see io.effi.rpc.context.context.ReplyInterceptor
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
