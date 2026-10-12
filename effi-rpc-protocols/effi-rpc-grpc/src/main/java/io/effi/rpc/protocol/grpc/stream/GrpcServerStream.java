package io.effi.rpc.protocol.grpc.stream;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.grpc.codec.GrpcHeaders;
import io.effi.rpc.protocol.grpc.codec.GrpcStatus;
import io.effi.rpc.protocol.grpc.codec.GrpcStatusMapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Headers;

import java.nio.charset.StandardCharsets;

/**
 * Provides the server half of one gRPC stream.
 */
public final class GrpcServerStream extends GrpcStream {

    public GrpcServerStream(Channel channel) {
        super(channel);
    }

    /**
     * Opens the stream by writing the gRPC response headers.
     */
    public void start() {
        Http2Headers headers = new DefaultHttp2Headers()
                .status(HttpResponseStatus.OK.codeAsText())
                .set(GrpcHeaders.CONTENT_TYPE, GrpcHeaders.CONTENT_TYPE_VALUE);
        channel().writeAndFlush(new DefaultHttp2HeadersFrame(headers, false));
    }

    @Override
    protected void writeCompletion() {
        Http2Headers trailers = new DefaultHttp2Headers()
                .set(GrpcHeaders.STATUS, String.valueOf(GrpcStatus.OK.code()));
        channel().writeAndFlush(new DefaultHttp2HeadersFrame(trailers, true));
    }

    @Override
    protected void writeFailure(EffiRpcException cause) {
        Http2Headers trailers = new DefaultHttp2Headers()
                .set(GrpcHeaders.STATUS, String.valueOf(GrpcStatusMapper.statusOf(cause).code()))
                .set(GrpcHeaders.MESSAGE, encodeMessage(cause.getMessage()));
        channel().writeAndFlush(new DefaultHttp2HeadersFrame(trailers, true));
    }

    private CharSequence encodeMessage(String message) {
        if (message == null) {
            return "";
        }
        StringBuilder encoded = new StringBuilder(message.length());
        for (int i = 0; i < message.length(); i++) {
            char value = message.charAt(i);
            if (value >= 0x20 && value <= 0x7E && value != '%') {
                encoded.append(value);
                continue;
            }
            for (byte b : String.valueOf(value).getBytes(StandardCharsets.UTF_8)) {
                encoded.append('%').append(String.format("%02X", b));
            }
        }
        return encoded;
    }
}
