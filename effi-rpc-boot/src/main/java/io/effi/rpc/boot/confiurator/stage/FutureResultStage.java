package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Client;

import java.net.InetSocketAddress;

import static io.effi.rpc.boot.confiurator.stage.FutureResultStage.NAME;

@Extension(NAME)
public class FutureResultStage implements Stage.CallUnit<Request, Caller<?>> {

    public static final String NAME = "futureResultStage";

    @Override
    public Interaction.Result process(CallContext<Request, Caller<?>> context, Chain chain) {
        Caller<?> caller = context.peer();
        Chain reverseStageChain = caller.replyStageChain();
        SmartURL requestUrl = context.message().url();
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(requestUrl.host(), requestUrl.port());
        TransportProtocol protocol = TransportSupport.findProtocol(caller);
        Client client = protocol.supplyClient(caller.clientConfig(), remoteAddress, context.platform());
        ReplyFuture future = context.mode().newFuture(context);
        CallAttempt attempt = new CallAttempt(future, client, protocol, context);
        future.onComplete(res -> {
            if (res.failed()) {
                return;
            }
            try {
                future.withRawResult(reverseStageChain.proceed(res.result()));
            } catch (Throwable e) {
                EffiRpcException failure = InteractionErrorCodes.REPLY_STAGE_FAILED.fail(e, caller.id());
                future.withRawResult(Interaction.Result.failure(context.message().url(), failure));
            }
        });
        attempt.start();
        return Interaction.Result.success(context.message().url(), future);
    }
}
