package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;

import java.util.Map;

/**
 * Handles one server-side request received from a channel.
 */
public final class ServerRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerRequestHandler.class);

    /**
     * Handles one server-side request message.
     *
     * @param inputMessage request message
     */
    public void handle(InputMessage inputMessage) {
        SmartURL smartUrl = inputMessage.url();
        Channel channel = inputMessage.channel();
        TransportProtocol protocol = channel.protocol();
        ServerExchange exchange = ServerExchange.of(inputMessage);
        try {
            ScopedModule module = protocol.lookupModule(inputMessage);
            if (module == null) {
                throw InteractionErrorCodes.SERVANT_NOT_FOUND.fail(smartUrl.baseUrl(), channel.remoteAddress());
            }
            Servant servant = module.namedComponent(Servant.class, Peer.buildId(smartUrl.scheme(), smartUrl.path()));
            if (servant == null) {
                exchange.fail(InteractionErrorCodes.SERVANT_NOT_FOUND.fail(
                        smartUrl.baseUrl(),
                        channel.remoteAddress()
                ));
                inputMessage.close();
                return;
            }
            servant.threadPool().execute(() -> invoke(inputMessage, protocol, servant, exchange))
                    .onComplete(result -> reject(result, servant, inputMessage, exchange));
        } catch (Throwable cause) {
            try (inputMessage) {
                exchange.fail(toRpcException(cause, inputMessage));
            }
        }
    }

    private static void invoke(InputMessage inputMessage, TransportProtocol protocol, Servant servant, ServerExchange exchange) {
        try (inputMessage) {
            ServerExchangeContextCodec serverCodec = protocol.serverCodec();
            CallContext<Request, Servant> callContext = serverCodec.decode(inputMessage, servant);
            Interaction.Result result = servant.callStageChain().proceed(callContext);
            Response response = protocol.createResponse(servant, callContext.message(), result);
            ReplyContext<Response, Servant> replyContext = new ReplyContext<>(callContext, response, result);
            servant.replyStageChain().proceed(replyContext);
            if (callContext.message().needReply()) {
                exchange.reply(replyContext);
            }
        } catch (Throwable cause) {
            exchange.fail(toRpcException(cause, servant));
        }
    }

    private static void reject(Result<Void> result, Servant servant, InputMessage inputMessage, ServerExchange exchange) {
        if (result.succeeded()) return;
        EffiRpcException failure = InteractionErrorCodes.SERVER_OVERLOADED
                .fail(result.cause(), servant.id())
                .withMetadata(Map.of(KeyConstant.RETRYABLE, Boolean.TRUE.toString()));
        try (inputMessage) {
            exchange.fail(failure);
        }
        logger.error(failure.getMessage(), failure);
    }

    private static EffiRpcException toRpcException(Throwable cause, Servant servant) {
        return cause instanceof EffiRpcException exception
                ? exception
                : InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(cause, servant.id());
    }

    private static EffiRpcException toRpcException(Throwable cause, InputMessage inputMessage) {
        return cause instanceof EffiRpcException exception
                ? exception
                : TransportErrorCodes.DECODE.fail(cause, Request.class, inputMessage.getClass());
    }
}
