package io.effi.rpc.transport.netty;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.transport.ChannelCallBindings;
import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.util.AssertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Provides Netty buffer, channel, and configuration helpers.
 */
public final class NettySupport {

    private static final AttributeKey<Long> FUTURE_ID = AttributeKey.valueOf("futureId");

    private NettySupport() {
    }

    /**
     * Creates a channel initializer that applies the supplied configuration.
     *
     * @param configure channel configuration
     * @param <T>       channel type
     * @return channel initializer
     */
    public static <T extends Channel> ChannelInitializer<T> newChannelInitializer(Consumer<T> configure) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(T channel) {
                configure.accept(channel);
            }
        };
    }

    /**
     * Creates a write buffer water mark from the endpoint configuration.
     *
     * @param config endpoint configuration
     * @return write buffer water mark
     */
    public static WriteBufferWaterMark newWriteBufferWaterMark(EndpointConfig config) {
        int low = Math.max(0, config.option(TransportOptions.WRITE_BUFFER_LOW_WATER_MARK));
        int high = Math.max(low, config.option(TransportOptions.WRITE_BUFFER_HIGH_WATER_MARK));
        return new WriteBufferWaterMark(low, high);
    }

    /**
     * Creates a byte buffer output stream for the channel.
     *
     * @param channel channel owning the buffer
     * @return byte buffer output stream
     */
    public static ByteBufOutputStream newOutputStream(NettyChannel channel) {
        AssertUtil.notNull(channel, "channel");
        return new ByteBufOutputStream(channel.channel().alloc().buffer());
    }

    /**
     * Returns the byte buffer backing the output stream.
     *
     * @param outputStream output stream
     * @return backing byte buffer
     */
    public static ByteBuf toByteBuf(OutputStream outputStream) {
        if (outputStream == null) return Unpooled.EMPTY_BUFFER;
        if (outputStream instanceof ByteBufOutputStream bufOutputStream) {
            return bufOutputStream.buffer();
        }
        return Unpooled.EMPTY_BUFFER;
    }

    /**
     * Creates an input stream over the byte buffer.
     *
     * @param buf source buffer
     * @return byte buffer input stream
     */
    public static ByteBufInputStream newInputStream(ByteBuf buf) {
        return new ByteBufInputStream(buf, true);
    }

    /**
     * Copies the readable bytes and releases the buffer.
     *
     * @param buf source buffer
     * @return copied bytes
     */
    public static byte[] getBytes(ByteBuf buf) {
        try {
            return io.netty.buffer.ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false);
        } finally {
            ReferenceCountUtil.release(buf);
        }
    }


    /**
     * Binds the in-flight call id to the channel.
     *
     * @param futureId reply future id
     * @param channel  target channel
     */
    public static void bindFutureId(Long futureId, Channel channel) {
        channel.attr(FUTURE_ID).set(futureId);
    }

    /**
     * Unbinds and releases the in-flight call id from the channel.
     *
     * @param channel target channel
     */
    public static void unbindFutureId(Channel channel) {
        Long futureId = channel.attr(FUTURE_ID).getAndSet(null);
        if (futureId == null) {
            return;
        }
        NettyChannel nettyChannel = NettyChannel.ensure(channel);
        ChannelCallBindings bindings = nettyChannel.platform().singleComponent(ChannelCallBindings.class);
        if (bindings != null) {
            bindings.unbind(futureId, nettyChannel);
        }
    }

    /**
     * Returns the reply future bound to the channel.
     *
     * @param channel target channel
     * @return bound reply future, or {@code null} when absent
     */
    public static ReplyFuture getBoundFuture(Channel channel) {
        Long futureId = channel.attr(FUTURE_ID).get();
        if (futureId == null) {
            return null;
        }
        return ReplyFuture.lookup(NettyChannel.ensure(channel).platform(), futureId);
    }

    /**
     * Builds the request URL for the channel path.
     *
     * @param channel target channel
     * @param path    request path
     * @return request URL
     */
    public static SmartURL createRequestUrl(NettyChannel channel, String path) {
        SmartURL requestSmartUrl = SmartURL.builder()
                .scheme(channel.protocol().name())
                .address(channel.localAddress())
                .path(path)
                .build();
        requestSmartUrl.addQueryParam(KeyConstant.ONEWAY, Boolean.FALSE.toString());
        return requestSmartUrl;
    }
}
