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

    public static WriteBufferWaterMark newWriteBufferWaterMark(EndpointConfig config) {
        int low = Math.max(0, config.option(TransportOptions.WRITE_BUFFER_LOW_WATER_MARK));
        int high = Math.max(low, config.option(TransportOptions.WRITE_BUFFER_HIGH_WATER_MARK));
        return new WriteBufferWaterMark(low, high);
    }

    public static ByteBufOutputStream newOutputStream(NettyChannel channel) {
        AssertUtil.notNull(channel, "channel");
        return new ByteBufOutputStream(channel.channel().alloc().buffer());
    }

    public static ByteBuf toByteBuf(OutputStream outputStream) {
        if (outputStream == null) return Unpooled.EMPTY_BUFFER;
        if (outputStream instanceof ByteBufOutputStream bufOutputStream) {
            return bufOutputStream.buffer();
        }
        return Unpooled.EMPTY_BUFFER;
    }

    public static ByteBufInputStream newInputStream(ByteBuf buf) {
        return new ByteBufInputStream(buf, true);
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


    public static void bindFutureId(Long futureId, Channel channel) {
        channel.attr(FUTURE_ID).set(futureId);
    }

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

    public static ReplyFuture getBoundFuture(Channel channel) {
        Long futureId = channel.attr(FUTURE_ID).get();
        if (futureId == null) {
            return null;
        }
        return ReplyFuture.lookup(NettyChannel.ensure(channel).platform(), futureId);
    }

    /**
     * Builds request config.
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
