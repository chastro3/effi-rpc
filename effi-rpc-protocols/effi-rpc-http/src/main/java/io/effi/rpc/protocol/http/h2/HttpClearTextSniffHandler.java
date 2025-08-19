package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.nativetools.NativeConfig;
import io.effi.rpc.util.AssertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http2.Http2CodecUtil.connectionPrefaceBuf;

/**
 * Detects the HTTP/2 clear text preface or processes the request as HTTP/1.1.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7540#section-3.5">RFC 7540 3.5</a>
 */
@NativeConfig.Reflect(typeReached = Http2Protocol.class, queryAllPublicMethods = true)
public class HttpClearTextSniffHandler extends ChannelInboundHandlerAdapter {

    private static final ByteBuf H2_PREFACE_BUF = unreleasableBuffer(connectionPrefaceBuf()).asReadOnly();

    private static final int H2_PREFACE_LENGTH = H2_PREFACE_BUF.readableBytes();

    private final HttpCombineChannelConfigurer configurer;

    private CompositeByteBuf compositeByteBuf;

    public HttpClearTextSniffHandler(HttpCombineChannelConfigurer configurer) {
        this.configurer = AssertUtil.notNull(configurer,"configurer");
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
        boolean isHttp2 = ByteBufUtil.equals(H2_PREFACE_BUF, byteBuf.slice(0, H2_PREFACE_LENGTH));
        ChannelPipeline pipeline = ctx.pipeline();
        Channel channel = ctx.channel();
        configurer.configureChannel(channel, isHttp2);
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