package io.effi.rpc.protocol.http.support;

import io.effi.rpc.constant.Component;
import io.effi.rpc.protocol.http.h1.NettyHttp1Headers;
import io.effi.rpc.protocol.http.h2.NettyHttp2Headers;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines supported HTTP protocol versions.
 */
public enum HttpVersion {

    HTTP_1_1(Component.Protocol.HTTP_1_1, NettyHttp1Headers::new, NettyHttp1Headers::new),

    HTTP_2_0(Component.Protocol.HTTP_2, NettyHttp2Headers::new, NettyHttp2Headers::new);

    private final String name;

    private final Supplier<HttpHeaders> noArgHeadersFactory;

    private final Function<Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>>, HttpHeaders> headersFactory;

    HttpVersion(String name, Supplier<HttpHeaders> noArgHeadersFactory, Function<Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>>, HttpHeaders> headersFactory) {
        this.name = name;
        this.noArgHeadersFactory = noArgHeadersFactory;
        this.headersFactory = headersFactory;
    }

    public String protocolName() {
        return name;
    }

    public HttpHeaders createHeaders() {
        return noArgHeadersFactory.get();
    }

    public HttpHeaders createHeaders(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
        return headersFactory.apply(headers);
    }
}
