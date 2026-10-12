package io.effi.rpc.protocol.grpc.stream;

import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.grpc.codec.GrpcHeaders;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Headers;

/**
 * Provides the client half of one gRPC stream.
 */
public final class GrpcClientStream extends GrpcStream {

    public GrpcClientStream(Channel channel) {
        super(channel);
    }

    /**
     * Opens the stream by writing the gRPC request headers.
     *
     * @param request call request
     */
    public void start(Request request) {
        Http2Headers headers = new DefaultHttp2Headers()
                .method(HttpMethod.POST.name())
                .path(request.url().queryPath())
                .scheme("http")
                .set(GrpcHeaders.CONTENT_TYPE, GrpcHeaders.CONTENT_TYPE_VALUE)
                .set(GrpcHeaders.TE, GrpcHeaders.TE_VALUE);
        channel().writeAndFlush(new DefaultHttp2HeadersFrame(headers, false));
    }

    @Override
    protected void writeCompletion() {
        channel().writeAndFlush(new DefaultHttp2DataFrame(Unpooled.EMPTY_BUFFER, true));
    }

    @Override
    protected void writeFailure(EffiRpcException cause) {
        channel().close();
    }
}
