package io.effi.rpc.transport;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;

/**
 * Orchestrates the server-side response for one received message.
 */
public final class ServerExchange {

    private static final Logger logger = LoggerFactory.getLogger(ServerExchange.class);

    private final InputMessage inputMessage;

    private final Channel channel;

    private final TransportProtocol protocol;

    private ServerExchange(InputMessage inputMessage) {
        this.inputMessage = inputMessage;
        this.channel = inputMessage.channel();
        this.protocol = channel.protocol();
    }

    public static ServerExchange of(InputMessage inputMessage) {
        return new ServerExchange(inputMessage);
    }

    public Future<Void> reply(ReplyContext<Response, Servant> context) {
        return send(EncodableOutputMessage.create(context, channel, protocol.serverCodec()));
    }

    public Future<Void> fail(EffiRpcException cause) {
        if (!(inputMessage instanceof Request request) || !request.needReply()) {
            return Futures.completedVoid();
        }
        Response response = protocol.createErrorResponse(inputMessage, cause);
        OutputMessage outputMessage = protocol.serverCodec().encode(response, channel);
        return send(outputMessage);
    }

    private Future<Void> send(OutputMessage outputMessage) {
        Future<Void> result = channel.send(outputMessage);
        result.onComplete(completion -> {
            if (completion.failed()) {
                logger.error("Failed to send server response to '{}'.", completion.cause(), channel.remoteAddress());
            }
        });
        return result;
    }
}
