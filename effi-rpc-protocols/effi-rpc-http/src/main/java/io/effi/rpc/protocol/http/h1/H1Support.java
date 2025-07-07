package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.URL;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.ApplicationProtocolNames;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

/**
 * Utility class for http1 operations.
 */
public class H1Support {

    public static final String[] SUPPORTED_PROTOCOL = new String[]{ApplicationProtocolNames.HTTP_1_1};

    /**
     * Converts from netty's full http request.
     */
    public static io.effi.rpc.protocol.http.support.HttpRequest<ByteBuf> fromFullHttpRequest(NettyChannel channel, FullHttpRequest request) {
        return io.effi.rpc.protocol.http.support.HttpRequest.<ByteBuf>builder()
                .version(HttpVersion.HTTP_1_1)
                .method(request.method())
                .url(NettySupport.createRequestUrl(channel, request.uri()))
                .headers(request.headers())
                .body(request.content())
                .build();
    }

    /**
     * Converts from netty's full http response.
     */
    public static HttpResponse<ByteBuf> fromFullHttpResponse(FullHttpResponse response, CallContext<Message.Request, Caller<?>> context) {
        Http1Caller<?> caller = (Http1Caller<?>) context.callSide();
        return HttpResponse.<ByteBuf>builder()
                .version(HttpVersion.HTTP_1_1)
                .method(caller.httpMethod())
                .statusCode(response.status().code())
                .url(context.message().url())
                .headers(response.headers())
                .body(response.content())
                .build();
    }

    /**
     * Converts to netty's full http request.
     */
    public static FullHttpRequest toFullHttpRequest(io.effi.rpc.protocol.http.support.HttpRequest<byte[]> request) {
        URL requestUrl = request.url();
        return new DefaultFullHttpRequest(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                request.method(),
                requestUrl.queryPath(),
                Unpooled.wrappedBuffer(request.body()),
                ((NettyHttp1Headers) request.headers()).headers(),
                trailersFactory().newHeaders()
        );
    }

    /**
     * Converts to netty's full http response.
     */
    public static FullHttpResponse toFullHttpResponse(HttpResponse<byte[]> response) {
        return new DefaultFullHttpResponse(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.statusCode()),
                Unpooled.wrappedBuffer(response.body()),
                ((NettyHttp1Headers) response.headers()).headers(),
                trailersFactory().newHeaders()
        );
    }

}
