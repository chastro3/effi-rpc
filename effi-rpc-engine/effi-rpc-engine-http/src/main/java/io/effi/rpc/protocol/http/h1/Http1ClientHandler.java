package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.protocol.http.URLBinderChannelHandler;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.util.LazyInitializer;
import io.effi.rpc.util.NetUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.effi.rpc.constant.Component.Protocol.HTTP;
import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Http client message converter.
 */
@Sharable
public final class Http1ClientHandler extends URLBinderChannelHandler {

    private static final LazyInitializer<NamedChannelHandler> LAZY_INITIALIZER = new LazyInitializer<>(
            () -> new NamedChannelHandler("httpClientHandler", new Http1ClientHandler())
    );

    private static final Object LOCK = new Object();

    private volatile Http1Protocol protocol;

    private Http1ClientHandler() {
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
    protected void readHttpResponse(ChannelHandlerContext ctx, Object msg, InvocationContext<Envelope.Request, Caller<?>> context) throws Exception {
        if (msg instanceof FullHttpResponse fullHttpResponse) {
            msg = H1Support.fromFullHttpResponse(fullHttpResponse, context.envelope().url());
            ctx.fireChannelRead(msg);
            Channel channel = ctx.channel();
            NettySupport.unbindURL(channel);
            NettyChannel nettyChannel = NettyChannel.get(channel);
            if (nettyChannel != null) {
                Http1Client http1Client = protocol().getClient(NetUtil.toAddress(nettyChannel.remoteAddress()));
                if (http1Client != null) {
                    http1Client.release(channel);
                }
            }
        }
    }

    private Http1Protocol protocol() {
        if (protocol == null) {
            synchronized (LOCK) {
                if (protocol == null) {
                    protocol = (Http1Protocol) TransportSupport.getProtocol(HTTP);
                }
            }
        }
        return protocol;
    }

    public static NamedChannelHandler getInstance() {
        return LAZY_INITIALIZER.get(false);
    }
}
