package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.protocol.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

/**
 * @Author WenBo Zhou
 * @Date 2024/11/22 14:27
 */
public class H1Support {

    /**
     * Converts from netty's full http request.
     *
     * @param endpointUrl
     * @param request
     * @return
     */
    public static io.effi.rpc.protocol.http.support.HttpRequest<ByteBuf> fromFullHttpRequest(URL endpointUrl, FullHttpRequest request) {
        return io.effi.rpc.protocol.http.support.HttpRequest.builder()
                .version(HttpVersion.HTTP_1_1)
                .method(request.method())
                .url(NettySupport.buildRequestUrl(endpointUrl, request.uri()))
                .headers(fromHttpHeaders(request.headers()))
                .body(request.content())
                .build();
    }

    /**
     * Converts from netty's full http response.
     *
     * @param response
     * @param requestUrl
     * @return
     */
    public static HttpResponse<ByteBuf> fromFullHttpResponse(FullHttpResponse response, URL requestUrl) {
        return HttpResponse.builder()
                .version(HttpVersion.HTTP_1_1)
                .method(null)
                .statusCode(response.status().code())
                .url(requestUrl)
                .headers(fromHttpHeaders(response.headers()))
                .body(response.content())
                .build();
    }

    /**
     * Converts to netty's full http request.
     *
     * @param request
     * @return
     */
    public static FullHttpRequest toFullHttpRequest(io.effi.rpc.protocol.http.support.HttpRequest<byte[]> request) {
        URL requestUrl = request.url();
        return new DefaultFullHttpRequest(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                request.method(),
                requestUrl.queryPath(),
                Unpooled.wrappedBuffer(request.body()),
                toHttpHeaders(request.headers()),
                trailersFactory().newHeaders()
        );
    }

    /**
     * Converts to netty's full http response.
     *
     * @param response
     * @return
     */
    public static FullHttpResponse toFullHttpResponse(HttpResponse<byte[]> response) {
        return new DefaultFullHttpResponse(
                io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.statusCode()),
                Unpooled.wrappedBuffer(response.body()),
                toHttpHeaders(response.headers()),
                trailersFactory().newHeaders()
        );
    }

    /**
     * Converts from netty's http headers.
     *
     * @param headers
     * @return
     */
    public static io.effi.rpc.protocol.http.support.HttpHeaders fromHttpHeaders(HttpHeaders headers) {
        var httpHeaders = new io.effi.rpc.protocol.http.support.DefaultHttpHeaders();
        headers.forEach(item -> httpHeaders.add(item.getKey(), item.getValue()));
        return httpHeaders;
    }

    /**
     * Converts to netty's http headers.
     *
     * @param headers
     * @return
     */
    public static HttpHeaders toHttpHeaders(io.effi.rpc.protocol.http.support.HttpHeaders headers) {
        var httpHeaders = new io.netty.handler.codec.http.DefaultHttpHeaders();
        headers.forEach(item -> httpHeaders.add(item.getKey(), item.getValue()));
        return httpHeaders;
    }

}
