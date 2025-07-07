package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.URL;
import io.effi.rpc.protocol.http.FutureBinder;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.effi.rpc.constant.Component.Protocol.HTTP_1_1;
import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles HTTP/1.1 client-side requests and responses.
 */
@Sharable
public final class Http1ClientHandler extends FutureBinder {

    private final Http1Protocol protocol = (Http1Protocol) TransportSupport.getProtocol(HTTP_1_1);

    private final Http1Client client;

    public Http1ClientHandler(Http1Client client) {
        this.client = AssertUtil.notNull(client, "client");
    }

    @Override
    protected URL supported(Object msg) {
        if (msg instanceof HttpRequest<?> httpRequest
                && httpRequest.body() instanceof byte[]) {
            return httpRequest.url();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void writeHttpRequest(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        HttpRequest<byte[]> request = (HttpRequest<byte[]>) msg;
        FullHttpRequest fullHttpRequest = H1Support.toFullHttpRequest(request);
        ctx.writeAndFlush(fullHttpRequest, promise);
    }

    @Override
    protected boolean readHttpResponse(ChannelHandlerContext ctx, Object msg, CallContext<Message.Request, Caller<?>> context) throws Exception {
        if (msg instanceof FullHttpResponse fullHttpResponse) {
            msg = H1Support.fromFullHttpResponse(fullHttpResponse, context);
            ctx.fireChannelRead(msg);
            client.release(ctx.channel());
        }
        return true;
    }
}
