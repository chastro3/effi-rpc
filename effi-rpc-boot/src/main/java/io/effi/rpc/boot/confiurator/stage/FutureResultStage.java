package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.support.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.message.EncodableOutputMessage;

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
        client.fetchChannel().onComplete(res -> {
            if (res.succeeded()) {
                Channel channel = res.result();
                var outputMessage = EncodableOutputMessage.create(context, channel, protocol.clientCodec());
                channel.send(outputMessage)
                        .onComplete(r -> {
                            if (r.failed()) {
                                EffiRpcException fail = TransportErrorCodes.CHANNEL_WRITE.fail(r.cause(), requestUrl.host());
                                future.failure(fail);
                            }
                        });

            } else {
                EffiRpcException fail = TransportErrorCodes.FETCH_CHANNEL
                        .fail(res.cause(), requestUrl.host(), requestUrl.scheme());
                future.failure(fail);
            }
        });
        future.onComplete(res ->
                future.withRawResult(reverseStageChain.proceed(res.result()))
        );
        return Interaction.Result.success(context.message().url(), future);
    }
}
