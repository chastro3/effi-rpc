package io.effi.rpc.protocol.http;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.protocol.NettySupport;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Bind request url to current channel.
 */
public abstract class URLBinderChannelHandler extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        URL requestUrl;
        if ((requestUrl = supported(msg)) != null) {
            writeHttpRequest(ctx, msg, promise);
            // bind request url to current channel
            NettySupport.bindURL(requestUrl, ctx.channel());
        } else {
            super.write(ctx, msg, promise);
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        URL requestUrl = NettySupport.acquireBoundChannel(ctx.channel());
        if (requestUrl != null) {
            Long id = requestUrl.get(KeyConstant.ATTR_UNIQUE_ID);
            if (id != null) {
                ReplyFuture future = ReplyFuture.acquireFuture(id);
                if (future != null) {
                    readHttpResponse(ctx, msg, future.context());
                }
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    protected abstract URL supported(Object msg);

    protected abstract void writeHttpRequest(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;

    protected abstract void readHttpResponse(ChannelHandlerContext ctx, Object msg, InvocationContext<Envelope.Request, Caller<?>> context) throws Exception;
}
