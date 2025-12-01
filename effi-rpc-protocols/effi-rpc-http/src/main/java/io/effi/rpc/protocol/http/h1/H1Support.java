package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
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
    public static HttpDuplexRequest fromFullHttpRequest(NettyChannel channel, FullHttpRequest request) {
        return HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(request.method())
                .url(NettySupport.createRequestUrl(channel, request.uri()))
                .headers(request.headers())
                .build()
                .input(NettySupport.newInputStream(request.content()));
    }

    /**
     * Converts from netty's full http response.
     */
    public static HttpResponse fromFullHttpResponse(FullHttpResponse response, CallContext<Request, Caller<?>> context) {
        Http1Caller<?> caller = (Http1Caller<?>) context.peer();
        return HttpDuplexResponse.builder()
                .version(Http1Protocol.VERSION)
                .method(caller.httpMethod())
                .statusCode(response.status().code())
                .url(context.message().url())
                .headers(response.headers())
                .build()
                .input(NettySupport.newInputStream(response.content()));
    }

    /**
     * Converts to netty's full http request.
     */
    public static FullHttpRequest toFullHttpRequest(HttpDuplexRequest request) {
        SmartURL requestSmartUrl = request.url();
        return new DefaultFullHttpRequest(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                request.method(),
                requestSmartUrl.queryPath(),
                NettySupport.toByteBuf(request.outputStream()),
                ((NettyHttp1Headers) request.headers()).headers(),
                trailersFactory().newHeaders()
        );
    }

    /**
     * Converts to netty's full http response.
     */
    public static FullHttpResponse toFullHttpResponse(HttpDuplexResponse response) {
        return new DefaultFullHttpResponse(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.statusCode()),
                NettySupport.toByteBuf(response.outputStream()),
                ((NettyHttp1Headers) response.headers()).headers(),
                trailersFactory().newHeaders()
        );
    }


}
