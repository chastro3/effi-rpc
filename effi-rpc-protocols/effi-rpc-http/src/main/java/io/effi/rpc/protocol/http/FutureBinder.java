package io.effi.rpc.protocol.http;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Binds future id to current channel.
 */
public abstract class FutureBinder extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        SmartURL requestSmartUrl;
        if ((requestSmartUrl = supports(msg)) != null) {
            writeHttpRequest(ctx, msg, promise);
            Long futureId = requestSmartUrl.get(KeyConstant.ATTR_UNIQUE_ID);
            NettySupport.bindFutureId(futureId, ctx.channel());
        } else {
            super.write(ctx, msg, promise);
        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ReplyFuture future = NettySupport.getBoundFuture(ctx.channel());
        if (future != null) {
            boolean completed = readHttpResponse(ctx, msg, future.context());
            if (completed) NettySupport.unbindFutureId(ctx.channel());
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettySupport.unbindFutureId(ctx.channel());
        super.exceptionCaught(ctx, cause);
    }

    protected abstract SmartURL supports(Object msg);

    protected abstract void writeHttpRequest(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception;

    protected abstract boolean readHttpResponse(ChannelHandlerContext ctx, Object msg, CallContext<Request, Caller<?>> context) throws Exception;
}
