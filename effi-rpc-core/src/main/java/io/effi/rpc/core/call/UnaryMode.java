package io.effi.rpc.core.call;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Client;

import java.net.InetSocketAddress;

/**
 * Provides the unary interaction strategy backed by one reply future.
 */
public final class UnaryMode implements Interaction.Mode {

    public static final UnaryMode INSTANCE = new UnaryMode();

    private UnaryMode() {
    }

    /**
     * Creates the reply future that carries the unary reply.
     *
     * @param context call context
     * @return reply future
     */
    public ReplyFuture newHandle(CallContext<Request, Caller<?>> context) {
        return new ReplyFuture(context);
    }

    @Override
    public Interaction.Result dispatch(CallContext<Request, Caller<?>> context) {
        Caller<?> caller = context.peer();
        Stage.Chain reverseStageChain = caller.replyStageChain();
        SmartURL requestUrl = context.message().url();
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(requestUrl.host(), requestUrl.port());
        TransportProtocol protocol = TransportSupport.findProtocol(caller);
        Client client = protocol.supplyClient(caller.clientConfig(), remoteAddress, context.platform());
        ReplyFuture replyFuture = newHandle(context);
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
        return Interaction.Result.success(requestUrl, replyFuture);
    }
}
