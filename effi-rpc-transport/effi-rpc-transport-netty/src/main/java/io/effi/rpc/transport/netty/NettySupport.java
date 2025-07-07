package io.effi.rpc.transport.netty;

import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Provides netty operations.
 */
public class NettySupport {

    private static final AttributeKey<Long> FUTURE_ID = AttributeKey.valueOf("futureId");

    public static <T extends Channel> ChannelInitializer<T> newChannelInitializer(Consumer<T> configure) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(T channel) {
                configure.accept(channel);
            }
        };
    }
    /**
     * Converts ByteBuf to byte array.
     */
    public static byte[] getBytes(ByteBuf buf) {
        try {
            return io.netty.buffer.ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false);
        } finally {
            ReferenceCountUtil.release(buf);
        }
    }

    public static CompletableFuture<io.effi.rpc.transport.endpoint.Channel> wrap(ChannelFuture future, Endpoint endpoint) {
        return wrapInternal(future, endpoint, ChannelFuture::channel);
    }

    public static CompletableFuture<io.effi.rpc.transport.endpoint.Channel> wrap(Future<? extends Channel> future, Endpoint endpoint) {
        return wrapInternal(future, endpoint, result -> future.getNow());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Future<?>> CompletableFuture<io.effi.rpc.transport.endpoint.Channel> wrapInternal(T future, Endpoint endpoint, java.util.function.Function<T, Channel> channelExtractor) {
        CompletableFuture<io.effi.rpc.transport.endpoint.Channel> promise = new CompletableFuture<>();
        future.addListener(result -> {
            T typedResult = (T) result;
            if (typedResult.isSuccess()) {
                try {
                    Channel channel = channelExtractor.apply(typedResult);
                    NettyChannel nettyChannel = NettyChannel.get(channel);
                    if (nettyChannel != null) {
                        promise.complete(nettyChannel);
                    } else {
                        promise.completeExceptionally(wrapChannelException(null, endpoint));
                    }
                } catch (Exception e) {
                    promise.completeExceptionally(wrapChannelException(e, endpoint));
                }
            } else {
                promise.completeExceptionally(wrapChannelException(typedResult.cause(), endpoint));
            }
        });
        long connectTimeout = endpoint.url().getLongParam(DefaultConfigNames.CONNECT_TIMEOUT);
        return promise.orTimeout(connectTimeout, TimeUnit.MILLISECONDS);
    }


    /**
     * Gets or creates ssl context.
     */
    public static SslContext getOrCreateSslContext(EndpointConfig config, Supplier<SslContext> creator) {
        boolean sslEnabled = config.getBooleanParam(DefaultConfigNames.SSL.realName(), false);
        return sslEnabled ? creator.get() : null;
    }

    public static void bindFutureId(Long futureId, Channel channel) {
        channel.attr(FUTURE_ID).set(futureId);
    }

    public static void unbindFutureId(Channel channel) {
        channel.attr(FUTURE_ID).set(null);
    }

    public static ReplyFuture getBoundFuture(Channel channel) {
        Long futureId = channel.attr(FUTURE_ID).get();
        return futureId == null ? null : ReplyFuture.getFuture(futureId);
    }

    /**
     * Builds request config.
     */
    public static URL createRequestUrl(NettyChannel channel, String path) {
        URL requestUrl = URL.builder()
                .type(URLType.REQUEST)
                .protocol(channel.protocol().protocol())
                .address(channel.localAddress())
                .path(path)
                .build();
        requestUrl.addParam(KeyConstant.ONEWAY, Boolean.FALSE.toString());
        return requestUrl;
    }

    private static EffiRpcException wrapChannelException(Throwable cause, Endpoint endpoint) {
        URL url = endpoint.url();
        return PredefinedErrorCode.GET_CHANNEL.fail(cause, url.authority(), url.protocol());
    }
}
