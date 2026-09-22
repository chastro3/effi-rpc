package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.transport.message.InputMessage;

/**
 * Provides transport layer operations.
 */
public class TransportSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransportSupport.class);

    public static TransportProtocol findProtocol(Peer peer) {
        if (peer.protocol() instanceof TransportProtocol transportProtocol) {
            return transportProtocol;
        }
        return peer.platform().namedComponent(TransportProtocol.class, peer.protocol().name());
    }

    public static boolean inIOSerialization(Peer peer) {
        try {
            Long serializationThreshold = peer.option(Peer.SERIALIZATION_THRESHOLD);
            if (serializationThreshold == null || serializationThreshold <= 0) return true;
            double averageSerializationTime;
            if (peer instanceof Caller<?>) {
                CallerMetrics callerMetrics = peer.get(CallerMetrics.GENERIC_KEY);
                averageSerializationTime = callerMetrics.averageSerializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = peer.get(CalleeMetrics.GENERIC_KEY);
                averageSerializationTime = calleeMetrics.averageSerializationTime().get();
            }
            return averageSerializationTime < serializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static void handleRequest(InputMessage inputMessage) {
        SmartURL smartUrl = inputMessage.url();
        Channel channel = inputMessage.channel();
        TransportProtocol protocol = channel.protocol();
        try {
            ScopedModule module = protocol.lookupModule(inputMessage);
            if (module == null) {
                throw InteractionErrorCodes.SERVANT_NOT_FOUND.fail(smartUrl.baseUrl(), channel.remoteAddress());
            }
            Servant servant = module.namedComponent(Servant.class, Peer.buildId(smartUrl.scheme(), smartUrl.path()));
            if (servant == null) {
                protocol.sendServantNotFound(inputMessage);
                inputMessage.close();
                return;
            }
            servant.threadPool().execute(() -> {
                try {
                    ServerExchangeContextCodec serverCodec = protocol.serverCodec();
                    CallContext<Request, Servant> callContext = serverCodec.decode(inputMessage, servant);
                    Interaction.Result result = servant.callStageChain().proceed(callContext);
                    Response response = protocol.createResponse(servant, result);
                    var replyContext = new ReplyContext<>(callContext, response, result);
                    servant.replyStageChain().proceed(replyContext);
                    if (callContext.message().needReply()) {
                        var outputMessage = EncodableOutputMessage.create(replyContext, channel, serverCodec);
                        channel.send(outputMessage);
                    }
                } catch (Throwable e) {
                    EffiRpcException failure = e instanceof EffiRpcException effiRpcException
                            ? effiRpcException
                            : InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(e, servant.id());
                    protocol.sendError(inputMessage, failure);
                } finally {
                    inputMessage.close();
                }
            }).onComplete(res -> {
                if (res.failed()) logger.error(res.cause());
            });
        } catch (Throwable e) {
            EffiRpcException failure = e instanceof EffiRpcException effiRpcException
                    ? effiRpcException
                    : TransportErrorCodes.DECODE.fail(e, Request.class, inputMessage.getClass());
            try (inputMessage) {
                protocol.sendError(inputMessage, failure);
            }
        }
    }

    public static void handleResponse(InputMessage inputMessage) {
        ReplyFuture future = ReplyFuture.lookup(inputMessage.url());
        Channel channel = inputMessage.channel();
        TransportProtocol protocol = channel.protocol();
        if (future != null) {
            ClientExchangeContextCodec clientCodec = protocol.clientCodec();
            Caller<?> caller = future.context().peer();
            ThreadPool threadPool = caller.threadPool();
            try {
                if (inIODeserialization(caller)) {
                    ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
                    inputMessage.close();
                    threadPool.execute(() -> future.complete(replyContext))
                            .onComplete(res -> {
                                if (res.failed()) logger.error(res.cause());
                            });
                } else {
                    threadPool.execute(() -> {
                        try {
                            ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
                            future.complete(replyContext);
                        } finally {
                            inputMessage.close();
                        }
                    });
                }
            } catch (Exception e) {
                inputMessage.close();
                EffiRpcException exception = TransportErrorCodes.CHANNEL_READ.fail(e, channel.remoteAddress());
                threadPool.execute(() -> future.failure(exception));
            }
        } else {
            inputMessage.close();
        }
    }

    public static boolean inIODeserialization(Peer peer) {
        try {
            Long deserializationThreshold = peer.option(Peer.DESERIALIZATION_THRESHOLD);
            if (deserializationThreshold == null || deserializationThreshold <= 0) return true;
            double averageDeserializationTime;
            if (peer instanceof Caller<?>) {
                CallerMetrics callerMetrics = peer.get(CallerMetrics.GENERIC_KEY);
                averageDeserializationTime = callerMetrics.averageDeserializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = peer.get(CalleeMetrics.GENERIC_KEY);
                averageDeserializationTime = calleeMetrics.averageDeserializationTime().get();
            }
            return averageDeserializationTime < deserializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
