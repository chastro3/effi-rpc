package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.context.ReplyStage;
import io.effi.rpc.base.context.StageChain;

import static io.effi.rpc.boot.stage.ReplyResultStage.NAME;


@Extension(NAME)
public class ReplyResultStage implements ReplyStage<Message.Response, Caller<?>> {

    public static final String NAME = "replyResultStage";

    @Override
    public Result execute(ReplyContext<Message.Response, Caller<?>> context, StageChain chain) {
        return context.result();
    }
}
