package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.netty.ChannelManageHandler;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http2.Http2CodecUtil.connectionPrefaceBuf;

/**
 * Detects the HTTP/2 cleartext preface or processes the request as HTTP/1.1.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7540#section-3.5">RFC 7540 §3.5</a>
 */
public class HttpClearTextSniffHandler extends ChannelInboundHandlerAdapter {

    private static final ByteBuf H2_PREFACE_BUF = unreleasableBuffer(connectionPrefaceBuf()).asReadOnly();

    private static final int H2_PREFACE_LENGTH = H2_PREFACE_BUF.readableBytes();

    private final HttpServerConfigurer configurer;

    private CompositeByteBuf compositeByteBuf;

    public HttpClearTextSniffHandler(ChannelManageHandler channelManager, NettyEndpointConfig h2config) {
        this.configurer = new HttpServerConfigurer(channelManager, h2config);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf request)) {
            super.channelRead(ctx, msg);
            return;
        }
        if (compositeByteBuf == null && request.readableBytes() >= H2_PREFACE_LENGTH) {
            configure(ctx, request);
        } else {
            if (compositeByteBuf == null) compositeByteBuf = ctx.alloc().compositeBuffer();
            compositeByteBuf.addComponents(true, request);
            if (compositeByteBuf.readableBytes() >= H2_PREFACE_LENGTH) {
                configure(ctx, compositeByteBuf);
            }
        }
    }

    private void configure(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        boolean isH2 = ByteBufUtil.equals(H2_PREFACE_BUF, byteBuf.slice(0, H2_PREFACE_LENGTH));
        ChannelPipeline pipeline = ctx.pipeline();
        configurer.configure(ctx.channel(), isH2);
        // If we were using compositeByteBuf, reset it
        if (byteBuf == compositeByteBuf) compositeByteBuf = null;
        ctx.fireChannelRead(byteBuf);
        pipeline.remove(this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (compositeByteBuf != null) {
            ReferenceCountUtil.safeRelease(compositeByteBuf);
            compositeByteBuf = null;
        }
    }
}