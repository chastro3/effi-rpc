package io.effi.rpc.protocol.http.support;

import io.effi.rpc.common.extension.GenericKey;

import static io.effi.rpc.common.constant.Component.Protocol.H2;
import static io.effi.rpc.common.constant.Component.Protocol.HTTP;

/**
 * Http version.
 */
public enum HttpVersion {

    HTTP_1_0("http1"),
    HTTP_1_1(HTTP),
    HTTP_2_0(H2);

    public static final GenericKey<HttpVersion> ATTRIBUTE_KEY = GenericKey.valueOf("httpVersion");

    private final String name;

    HttpVersion(String name) {
        this.name = name;
    }

    /**
     * Returns protocol name.
     *
     * @return
     */
    public String protocolName() {
        return name;
    }
}
