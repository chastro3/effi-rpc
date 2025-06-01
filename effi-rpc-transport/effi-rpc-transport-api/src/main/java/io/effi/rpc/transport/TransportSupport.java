package io.effi.rpc.transport;

import io.effi.rpc.base.*;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.util.AssertUtil;

import java.net.InetSocketAddress;

/**
 * Provides transport layer operations.
 */
public class TransportSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransportSupport.class);

    public static Protocol getProtocol(String name) {
        AssertUtil.notBlank(name, "name");
        return ExtensionLoader.loadExtension(Protocol.class, name);
    }

    public static boolean inIOSerialization(Invoker<?> invoker) {
        try {
            String value = invoker.get(DefaultConfigKeys.SERIALIZATION_THRESHOLD);
            long serializationThreshold = Long.parseLong(value);
            if (serializationThreshold == 0) return true;
            double averageSerializationTime;
            if (invoker instanceof Caller<?>) {
                CallerMetrics callerMetrics = invoker.get(CallerMetrics.GENERIC_KEY);
                averageSerializationTime = callerMetrics.averageSerializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = invoker.get(CalleeMetrics.GENERIC_KEY);
                averageSerializationTime = calleeMetrics.averageSerializationTime().get();
            }
            return averageSerializationTime < serializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static boolean inIODeserialization(Invoker<?> invoker) {
        try {
            String value = invoker.get(DefaultConfigKeys.DESERIALIZATION_THRESHOLD);
            long deserializationThreshold = Long.parseLong(value);
            if (deserializationThreshold == 0) return true;
            double averageDeserializationTime;
            if (invoker instanceof Caller<?>) {
                CallerMetrics callerMetrics = invoker.get(CallerMetrics.GENERIC_KEY);
                averageDeserializationTime = callerMetrics.averageDeserializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = invoker.get(CalleeMetrics.GENERIC_KEY);
                averageDeserializationTime = calleeMetrics.averageDeserializationTime().get();
            }
            return averageDeserializationTime < deserializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static <T extends ReplyFuture> T sendRequest(Protocol protocol, T future) {
        var context = future.context();
        Caller<?> caller = context.invoker();
        URL requestUrl = context.envelope().url();
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
                                EffiRpcException fail = PredefinedErrorCode.CHANNEL_WRITE.fail(e, requestUrl.toString());
                                future.complete(fail);
                            } else {
                                future.startTimeout();
                            }
                        });
            }
        });
        return future;
    }

    public static void handleRequest(Envelope.Request request, Channel channel) {
        EffiRpcPlatform platform = channel.platform();
        URL url = request.url();
        String applicationName = url.getParam(KeyConstant.REQUEST_REMOTE_APPLICATION, Component.DEFAULT);
        String moduleName = url.getParam(KeyConstant.REQUEST_REMOTE_MODULE, Component.DEFAULT);
        EffiRpcApplication application = platform.getApplication(applicationName);
        EffiRpcModule module = application.getModule(moduleName);
        Callee<?> callee = module.lookup(Callee.class, InvokerContainer.invokerKey(url.protocol(), url.path()));
        // todo send to client
        if (callee == null) {
            channel.protocol().sendCalleeNotFound(request, channel);
        } else {
            callee.threadPool().execute(() -> {
                ServerCodec serverCodec = channel.protocol().serverCodec();
                WrappedRequest<Callee<?>> wrappedRequest = serverCodec.decode(channel, request, callee);
                var replyContext = callee.invokeWithContext(wrappedRequest.context());
                if (wrappedRequest.request().needReply()) {
                    channel.send(new DefaultWrappedResponse<>(replyContext, channel));
                }
            });
        }
    }

    public static void handleResponse(Envelope.Response response, Channel channel) {
        ReplyFuture future = ReplyFuture.getFuture(response.url());
        Protocol protocol = channel.protocol();
        if (future != null) {
            ClientCodec clientCodec = protocol.clientCodec();
            Caller<?> caller = future.context().invoker();
            ThreadPool threadPool = caller.threadPool();
            try {
                if (inIODeserialization(caller)) {
                    WrappedResponse<Caller<?>> wrappedResponse = clientCodec.decode(channel, response, future);
                    threadPool.execute(() -> future.complete(wrappedResponse.context()));
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
}
