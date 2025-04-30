package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;

import java.util.Collections;
import java.util.List;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Http2 Server Handler.
 */
@Sharable
public final class Http2ServerHandler extends ChannelDuplexHandler {

    private final URL serverUrl;

    private final EffiRpcModule module;

    private final List<NamedChannelHandler> handlers;

    public Http2ServerHandler(URL serverUrl, EffiRpcModule module) {
        this.serverUrl = serverUrl;
        this.module = module;
        this.handlers = Collections.singletonList(ServerMessageAggregator.getInstance());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        handlers.forEach(handler -> pipeline.addLast(handler.name(), handler.handler()));
        NettyChannel.getOrCreate(ctx.channel(), serverUrl, module);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse<?> response) {
            HttpResponse<byte[]> httpResponse = (HttpResponse<byte[]>) response;
            Http2StreamFrame[] frames = H2Support.toHttp2StreamFrames(httpResponse);
            for (Http2StreamFrame frame : frames) {
                ctx.write(frame, ctx.newPromise());
            }
            ctx.flush();
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Http2RequestStream requestStream = null;
        if (msg instanceof Http2HeadersFrame headersFrame) {
            requestStream = H2Support.getOrCreateRequestStream(ctx, headersFrame.stream());
            requestStream.parseHeaderFrame(headersFrame);
        } else if (msg instanceof Http2DataFrame dataFrame) {
            requestStream = H2Support.getOrCreateRequestStream(ctx, dataFrame.stream());
            requestStream.parseDataFrame(dataFrame);
        } else {
            super.channelRead(ctx, msg);
        }
        if (requestStream != null && requestStream.endStream()) {
            HttpRequest<ByteBuf> httpRequest = H2Support.fromHtt2RequestStream(requestStream);
            ctx.fireChannelRead(httpRequest);
            H2Support.removeRequestStream(ctx, requestStream);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
