package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerContainer;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.support.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
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
            Long serializationThreshold = peer.getConfig(ConfigNames.SERIALIZATION_THRESHOLD);
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
        ScopedModule module = protocol.lookupModule(inputMessage);
        Callee callee = module.namedComponent(Callee.class, PeerContainer.invokerKey(smartUrl.scheme(), smartUrl.path()));
        // todo send to client
        if (callee == null) {
            protocol.sendCalleeNotFound(inputMessage);
        } else {
            callee.threadPool().execute(() -> {
                ServerExchangeContextCodec serverCodec = protocol.serverCodec();
                CallContext<Request, Callee> callContext = serverCodec.decode(inputMessage, callee);
                Interaction.Result result = callee.callStageChain().proceed(callContext);
                Response response = protocol.createResponse(callee, result);
                var replyContext = new ReplyContext<>(callContext, response, result);
                callee.replyStageChain().proceed(replyContext);
                if (callContext.message().needReply()) {
                    var outputMessage = EncodableOutputMessage.create(replyContext, channel, serverCodec);
                    channel.send(outputMessage);
                }
            }).onComplete(res -> {
                if (res.failed()) logger.error(res.cause());
            });
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
                    threadPool.execute(() -> future.complete(replyContext))
                            .onComplete(res -> {
                                if (res.failed()) logger.error(res.cause());
                            });
                } else {
                    threadPool.execute(() -> {
                        ReplyContext<Response, Caller<?>> replyContext = clientCodec.decode(inputMessage, caller);
                        future.complete(replyContext);
                    });
                }
            } catch (Exception e) {
                EffiRpcException exception = PredefinedErrorCode.CHANNEL_READ.fail(e, channel.remoteAddress());
                threadPool.execute(() -> future.failure(exception));
            }
        }
    }

    public static boolean inIODeserialization(Peer peer) {
        try {
            Long deserializationThreshold = peer.getConfig(ConfigNames.DESERIALIZATION_THRESHOLD);
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
