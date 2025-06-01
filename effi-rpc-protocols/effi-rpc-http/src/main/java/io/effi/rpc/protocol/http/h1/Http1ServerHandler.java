package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.netty.NettyChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles HTTP/1.1 server-side requests and responses.
 */
@Sharable
public final class Http1ServerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest fullHttpRequest) {
            NettyChannel channel = NettyChannel.get(ctx.channel());
            if (channel != null) {
                msg = H1Support.fromFullHttpRequest(channel, fullHttpRequest);
            }
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

}
