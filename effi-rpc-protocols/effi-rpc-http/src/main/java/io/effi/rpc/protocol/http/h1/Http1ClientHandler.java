package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.protocol.http.FutureBinder;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Handles HTTP/1.1 client-side requests and responses.
 */
@NativeConfig.Reflect(typeReached = Http1Protocol.class, queryAllPublicMethods = true)
@Sharable
public final class Http1ClientHandler extends FutureBinder {

    private final Http1Client client;

    public Http1ClientHandler(Http1Client client) {
        this.client = AssertUtil.notNull(client, "client");
    }

    @Override
    protected SmartURL supports(Object msg) {
        if (msg instanceof HttpDuplexRequest request) {
            return request.url();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void writeHttpRequest(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        HttpDuplexRequest request = (HttpDuplexRequest) msg;
        FullHttpRequest fullHttpRequest = H1Support.toFullHttpRequest(request);
        ctx.writeAndFlush(fullHttpRequest, promise);
    }

    @Override
    protected boolean readHttpResponse(ChannelHandlerContext ctx, Object msg, CallContext<Request, Caller<?>> context) throws Exception {
        if (msg instanceof FullHttpResponse fullHttpResponse) {
            msg = H1Support.fromFullHttpResponse(fullHttpResponse, context);
            ctx.fireChannelRead(msg);
            client.release(ctx.channel());
        }
        return true;
    }
}
