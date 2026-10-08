package io.effi.rpc.boot.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.boot.call.CallAttempt;
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

import static io.effi.rpc.boot.stage.CallAttemptStage.NAME;

/**
 * Handles the call attempt stage for dispatching requests over a client channel.
 */
@Extension(NAME)
public class CallAttemptStage implements Stage.CallUnit<Request, Caller<?>> {

    public static final String NAME = "callAttemptStage";

    @Override
    public Interaction.Result process(CallContext<Request, Caller<?>> context, Chain chain) {
        Caller<?> caller = context.peer();
        Chain reverseStageChain = caller.replyStageChain();
        SmartURL requestUrl = context.message().url();
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(requestUrl.host(), requestUrl.port());
        TransportProtocol protocol = TransportSupport.findProtocol(caller);
        Client client = protocol.supplyClient(caller.clientConfig(), remoteAddress, context.platform());
        ReplyFuture replyFuture = context.mode().newFuture(context);
        CallAttempt attempt = new CallAttempt(replyFuture, client, protocol);
        replyFuture.onComplete(res -> {
            // Failed attempts carry no decoded reply; Unary.FailureHandler owns their outcome.
            if (res.failed()) {
                return;
            }
            try {
                replyFuture.withRawResult(reverseStageChain.proceed(res.value()));
            } catch (Throwable e) {
                EffiRpcException failure = InteractionErrorCodes.REPLY_STAGE_FAILED.fail(e, caller.id());
                replyFuture.withRawResult(Interaction.Result.failure(context.message().url(), failure));
            }
        });
        attempt.dispatch();
        return Interaction.Result.success(context.message().url(), replyFuture);
    }
}
