package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.protocol.http.HttpOptions;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.ApplicationProtocolNames;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

/**
 * Provides HTTP/1.1 message and codec conversion utilities.
 */
public final class H1Support {

    public static final String[] SUPPORTED_PROTOCOL = new String[]{ApplicationProtocolNames.HTTP_1_1};

    private H1Support() {
    }

    /**
     * Builds the Netty HTTP decoder configuration from endpoint options.
     */
    public static HttpDecoderConfig newDecoderConfig(EndpointConfig config) {
        return new HttpDecoderConfig()
                .setMaxInitialLineLength(config.option(Http1Options.MAX_INITIAL_LINE_LENGTH))
                .setMaxHeaderSize(config.option(Http1Options.MAX_HEADER_SIZE))
                .setMaxChunkSize(config.option(Http1Options.MAX_CHUNK_SIZE))
                .setInitialBufferSize(config.option(HttpOptions.DECODER_INITIAL_BUFFER_SIZE));
    }

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
                .channel(channel)
                .input(NettySupport.newInputStream(request.content()));
    }

    /**
     * Converts from netty's full http response.
     */
    public static HttpResponse fromFullHttpResponse(
            FullHttpResponse response,
            CallContext<Request, Caller<?>> context,
            NettyChannel channel
    ) {
        Http1Caller<?> caller = (Http1Caller<?>) context.peer();
        return HttpDuplexResponse.builder()
                .version(Http1Protocol.VERSION)
                .method(caller.httpMethod())
                .statusCode(response.status().code())
                .url(context.message().url())
                .headers(response.headers())
                .build()
                .channel(channel)
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
