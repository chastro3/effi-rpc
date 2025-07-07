package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.ResultType;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.CallStage;
import io.effi.rpc.base.context.StageChain;

import static io.effi.rpc.boot.stage.FutureResultStage.NAME;


@Extension(NAME)
public class FutureResultStage implements CallStage<Message.Request, Caller<?>> {

    public static final String NAME = "futureResultStage";

    @Override
    public Result execute(CallContext<Message.Request, Caller<?>> context, StageChain chain) {
        Caller<?> caller = context.callSide();
        StageChain reverseStageChain = caller.replyStageChain();
        ReplyFuture future = ReplyFuture.getFuture(context.message().url());
        if (future != null) {
            future.whenComplete(reverseStageChain::proceed);
            return ResultType.FUTURE.createResult(context.message().url(), future);
        }
        return null;
    }
}
