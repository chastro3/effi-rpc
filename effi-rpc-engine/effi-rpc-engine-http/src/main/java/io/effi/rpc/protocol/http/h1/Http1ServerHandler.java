package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.util.LazyInitializer;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Http server message converter.
 */
@Sharable
public final class Http1ServerHandler extends ChannelDuplexHandler {

    private static final LazyInitializer<NamedChannelHandler> LAZY_INITIALIZER = new LazyInitializer<>(
            () -> new NamedChannelHandler("httpServerHandler", new Http1ServerHandler())
    );

    private Http1ServerHandler() {
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest fullHttpRequest) {
            NettyChannel nettyChannel = NettyChannel.get(ctx.channel());
            msg = H1Support.fromFullHttpRequest(nettyChannel.url(), fullHttpRequest);
        }
        super.channelRead(ctx, msg);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse<?> response) {
            HttpResponse<byte[]> httpResponse = (HttpResponse<byte[]>) response;
            msg = H1Support.toFullHttpResponse(httpResponse);
        }
        super.write(ctx, msg, promise);
    }

    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.get(false);
    }

}
