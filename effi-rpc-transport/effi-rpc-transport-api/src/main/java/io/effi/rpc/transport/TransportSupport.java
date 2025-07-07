package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.CallSideContainer;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.URL;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.util.AssertUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * Provides transport layer operations.
 */
public class TransportSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransportSupport.class);

    public static Protocol getProtocol(String name) {
        AssertUtil.notBlank(name, "name");
        return EffiRpcPlatform.getInstance()
                .getExtension(Protocol.class, name);
    }

    public static boolean inIOSerialization(CallSide callSide) {
        try {
            String value = callSide.getConfig(DefaultConfigNames.SERIALIZATION_THRESHOLD);
            long serializationThreshold = Long.parseLong(value);
            if (serializationThreshold == 0) return true;
            double averageSerializationTime;
            if (callSide instanceof Caller<?>) {
                CallerMetrics callerMetrics = callSide.get(CallerMetrics.GENERIC_KEY);
                averageSerializationTime = callerMetrics.averageSerializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = callSide.get(CalleeMetrics.GENERIC_KEY);
                averageSerializationTime = calleeMetrics.averageSerializationTime().get();
            }
            return averageSerializationTime < serializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static <T extends ReplyFuture> T sendRequest(Protocol protocol, T future) {
        var context = future.context();
        Caller<?> caller = context.callSide();
        URL requestUrl = context.message().url();
        // todo 优化重复创建逻辑
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(requestUrl.host(), requestUrl.port());
        Client client = protocol.transporter().getClient(caller.clientConfig(), remoteAddress, context.platform());
        client.getChannel().whenComplete((channel, e) -> {
            if (e != null) {
                EffiRpcException fail = PredefinedErrorCode.GET_CHANNEL.fail(e, requestUrl.host(), requestUrl.protocol());
                future.complete(fail);
            } else {
                channel.send(new DefaultWrappedRequest<>(context, channel))
                        .whenComplete((v, t) -> {
                            if (t != null) {
                                EffiRpcException fail;
                                if (t instanceof TimeoutException) {
                                    String timeout = caller.getConfig(DefaultConfigNames.TIMEOUT);
                                    fail = PredefinedErrorCode.TIMEOUT.fail(t, timeout, future.id());
                                } else {
                                    fail = PredefinedErrorCode.CHANNEL_WRITE.fail(t, requestUrl.toString());
                                }
                                future.complete(fail);
                            } else {
                                future.startTimeout();
                            }
                        });
            }
        });
        return future;
    }

    public static void handleRequest(Message.Request request, Channel channel) {
        URL url = request.url();
        EffiRpcModule module = channel.protocol().getModule(request, channel);
        Callee callee = module.lookup(Callee.class, CallSideContainer.invokerKey(url.protocol(), url.path()));
        // todo send to client
        if (callee == null) {
            channel.protocol().sendCalleeNotFound(request, channel);
        } else {
            callee.threadPool().execute(() -> {
                ServerCodec serverCodec = channel.protocol().serverCodec();
                WrappedRequest<Callee> wrappedRequest = serverCodec.decode(channel, request, callee);
                var invocationContext = wrappedRequest.context();
                Result result = callee.callStageChain().proceed(invocationContext);
                Message.Response response = channel.protocol().createResponse(callee, result);
                var replyContext = new ReplyContext<>(invocationContext, response, result);
                // var replyContext = callee.invokeWithContext(wrappedRequest.context());
                if (wrappedRequest.request().needReply()) {
                    channel.send(new DefaultWrappedResponse<>(replyContext, channel));
                }
            }).exceptionally(e -> {
                logger.error(e);
                return null;
            });
        }
    }

    public static void handleResponse(Message.Response response, Channel channel) {
        ReplyFuture future = ReplyFuture.getFuture(response.url());
        Protocol protocol = channel.protocol();
        if (future != null) {
            ClientCodec clientCodec = protocol.clientCodec();
            Caller<?> caller = future.context().callSide();
            ThreadPool threadPool = caller.threadPool();
            try {
                if (inIODeserialization(caller)) {
                    WrappedResponse<Caller<?>> wrappedResponse = clientCodec.decode(channel, response, future);
                    threadPool.execute(() -> future.complete(wrappedResponse.context()))
                            .exceptionally(e -> {
                                logger.error(e);
                                return null;
                            });
                } else {
                    threadPool.execute(() -> {
                        WrappedResponse<Caller<?>> wrappedResponse = clientCodec.decode(channel, response, future);
                        future.complete(wrappedResponse.context());
                    });
                }
            } catch (Exception e) {
                EffiRpcException exception = PredefinedErrorCode.CHANNEL_READ.fail(e, channel.url().address());
                threadPool.execute(() -> future.complete(exception));
            }
        }
    }

    public static boolean inIODeserialization(CallSide callSide) {
        try {
            String value = callSide.getConfig(DefaultConfigNames.DESERIALIZATION_THRESHOLD);
            long deserializationThreshold = Long.parseLong(value);
            if (deserializationThreshold == 0) return true;
            double averageDeserializationTime;
            if (callSide instanceof Caller<?>) {
                CallerMetrics callerMetrics = callSide.get(CallerMetrics.GENERIC_KEY);
                averageDeserializationTime = callerMetrics.averageDeserializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = callSide.get(CalleeMetrics.GENERIC_KEY);
                averageDeserializationTime = calleeMetrics.averageDeserializationTime().get();
            }
            return averageDeserializationTime < deserializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
