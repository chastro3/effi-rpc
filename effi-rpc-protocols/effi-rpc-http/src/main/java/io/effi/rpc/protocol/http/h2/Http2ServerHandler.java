package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;

import static io.netty.channel.ChannelHandler.Sharable;

/**
 * Http2 Server Handler.
 */
@NativeConfig.Reflect(typeReached = Http2Protocol.class, queryAllPublicMethods = true)
@Sharable
public final class Http2ServerHandler extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpDuplexResponse response) {
            Http2StreamFrame[] frames = H2Support.toHttp2StreamFrames(response);
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
            HttpRequest httpRequest = H2Support.fromHtt2RequestStream(requestStream, ctx);
            ctx.fireChannelRead(httpRequest);
            H2Support.removeRequestStream(ctx, requestStream);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
