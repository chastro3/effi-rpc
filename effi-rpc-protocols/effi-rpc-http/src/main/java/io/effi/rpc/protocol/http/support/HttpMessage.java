package io.effi.rpc.protocol.http.support;

import io.effi.rpc.base.Message;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Represents an HTTP envelope containing protocol version, headers, method, and body content.
 */
public interface HttpMessage<BODY> extends Message {

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
     * Returns the message body.
     */
    BODY body();

    /**
     * Replaces the body and returns a new envelope with the updated body.
     */
    <NEW> HttpMessage<NEW> body(NEW body);

    @Override
    default boolean isInstance() {
        return !(body() instanceof ByteBuf) && !(body() instanceof byte[]);
    }
}


