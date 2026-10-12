package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcHeaders;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.codec.GrpcStatus;
import io.effi.rpc.protocol.grpc.codec.GrpcStatusMapper;
import io.effi.rpc.protocol.grpc.stream.GrpcClientStream;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2ResetFrame;

import java.util.List;

/**
 * Handles client-side HTTP/2 frames for one gRPC client stream.
 */
public final class GrpcClientHandler extends ChannelDuplexHandler {

    private final GrpcFraming.Decoder decoder;

    private GrpcClientStream stream;

    public GrpcClientHandler(int maxMessageSize) {
        this.decoder = new GrpcFraming.Decoder(maxMessageSize);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.stream = new GrpcClientStream(ctx.channel());
        ctx.channel().attr(GrpcSupport.CLIENT_STREAM).set(stream);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame headersFrame) {
            handleHeaders(headersFrame);
            return;
        }
        if (msg instanceof Http2DataFrame dataFrame) {
            handleData(dataFrame);
            return;
        }
        if (msg instanceof Http2ResetFrame resetFrame) {
            stream.inboundError(TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                    resetFrame.errorCode(),
                    ctx.channel().remoteAddress()
            ));
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        stream.writabilityChanged();
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        stream.inboundError(TransportErrorCodes.CHANNEL_INACTIVE.fail(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        stream.inboundError(TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                cause,
                ctx.channel().remoteAddress(),
                cause.getMessage()
        ));
        ctx.close();
    }

    private void handleHeaders(Http2HeadersFrame frame) {
        if (frame.headers().status() != null) {
            return;
        }
        CharSequence statusValue = frame.headers().get(GrpcHeaders.STATUS);
        GrpcStatus status = statusValue == null
                ? GrpcStatus.INTERNAL
                : GrpcStatus.fromCode(Integer.parseInt(statusValue.toString()));
        if (status == GrpcStatus.OK) {
            stream.inboundCompleted();
            return;
        }
        CharSequence message = frame.headers().get(GrpcHeaders.MESSAGE);
        stream.inboundError(GrpcStatusMapper.toException(status, message == null ? null : message.toString()));
    }

    private void handleData(Http2DataFrame frame) {
        try {
            List<GrpcMessage> messages = decoder.decode(frame.content());
            for (GrpcMessage message : messages) {
                stream.inboundMessage(message);
            }
        } finally {
            frame.release();
        }
        if (frame.isEndStream()) {
            stream.inboundCompleted();
        }
    }
}
