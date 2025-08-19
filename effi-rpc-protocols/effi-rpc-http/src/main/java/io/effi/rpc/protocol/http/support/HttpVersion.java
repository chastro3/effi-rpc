package io.effi.rpc.protocol.http.support;

import java.util.Map;

public interface HttpVersion {

    String name();

    HttpHeaders newHeaders();

    HttpHeaders newHeaders(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers);

    default String schema() {
        return "http";
    }

}
