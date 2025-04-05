package io.effi.rpc.transport;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.config.URLType;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.*;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransportSupport {

    private static final Map<String, Protocol> PROTOCOLS = new ConcurrentHashMap<>(8);

    public static Protocol getProtocol(String name) {
        AssertUtil.notBlank(name, "name");
        return PROTOCOLS.computeIfAbsent(name, key -> ExtensionLoader.loadExtension(Protocol.class, name));
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
        URL requestUrl = context.source().url();
        URL clientUrl = URL.builder()
                .type(URLType.CLIENT)
                .protocol(requestUrl.protocol())
                .address(requestUrl.address())
                .params(caller.clientConfig().config().items())
                .build();
        Client client = protocol.openClient(clientUrl, context.module());
        Channel channel = client.getChannel();
        channel.send(new DefaultRepackagedRequest<>(context, channel));
        return future;
    }

    public static void handleRequest(Envelope.Request request, Channel channel) {
        Callee<?> callee = channel.module()
                .serverExporterManager()
                .getCallee(request.url());
        // todo send to client
        if (callee == null) {
            throw PredefinedErrorCode.NOT_FOUND_CALLEE.fail(null, request.url().uri());
        }
        callee.threadPoolOf(channel.module()).execute(() -> {
            ServerCodec serverCodec = channel.protocol().serverCodec();
            RepackagedRequest<Callee<?>> repackagedRequest = serverCodec.decode(channel, request, callee);
            var replyContext = callee.invokeWithContext(repackagedRequest.context());
            if (repackagedRequest.request().needReply()) {
                channel.send(new DefaultRepackagedResponse<>(replyContext, channel));
            }
        });
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
                    RepackagedResponse<Caller<?>> repackagedResponse = clientCodec.decode(channel, response, future);
                    threadPool.execute(() -> future.complete(repackagedResponse.context()));
                } else {
                    threadPool.execute(() -> {
                        RepackagedResponse<Caller<?>> repackagedResponse = clientCodec.decode(channel, response, future);
                        future.complete(repackagedResponse.context());
                    });
                }
            } catch (Exception e) {
                EffiRpcException exception = PredefinedErrorCode.CHANNEL_READ.fail(e, channel.url().address());
                threadPool.execute(() -> future.complete(exception));
            }
        }
    }
}
