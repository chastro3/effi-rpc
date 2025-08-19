package io.effi.rpc.protocol.http.support;

import io.effi.rpc.context.Message;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Represents an HTTP message containing protocol version, headers, method, and body content.
 */
public interface HttpMessage extends Message {

    /**
     * Returns the HTTP version.
     */
    HttpVersion version();

    /**
     * Returns the HTTP method.
     */
    HttpMethod method();

    /**
     * Returns the HTTP headers.
     */
    HttpHeaders headers();

    /**
     * Returns the HTTP body.
     */
    <T> T body();
}


