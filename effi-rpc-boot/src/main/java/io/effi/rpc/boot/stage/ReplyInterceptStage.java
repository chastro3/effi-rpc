package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.context.ReplyStage;
import io.effi.rpc.base.context.StageChain;

import static io.effi.rpc.boot.stage.ReplyInterceptStage.NAME;

/**
 * Executes the reply interceptor chain during the reply phase.
 *
 * @see io.effi.rpc.base.context.ReplyInterceptor
 */
@Extension(NAME)
public class ReplyInterceptStage implements ReplyStage<Message.Response, Caller<?>> {

    public static final String NAME = "replyFilterStage";

    @Override
    public Result execute(ReplyContext<Message.Response, Caller<?>> context, StageChain chain) {
        return context.callSide()
                .replyInterceptorChain()
                .proceed(context);
    }
}
