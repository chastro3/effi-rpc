package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Stage;

import static io.effi.rpc.boot.confiurator.stage.ReplyResultStage.NAME;

@Extension(NAME)
public class ReplyResultStage implements Stage.ReplyUnit<Response, Peer> {

    public static final String NAME = "replyResultStage";

    @Override
    public Interaction.Result process(ReplyContext<Response, Peer> context, Chain chain) {
        return context.result();
    }
}
