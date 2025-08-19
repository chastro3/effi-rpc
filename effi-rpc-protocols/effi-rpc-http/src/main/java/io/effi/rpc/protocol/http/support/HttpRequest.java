package io.effi.rpc.protocol.http.support;

import io.effi.rpc.context.Request;

/**
 * Represents an HTTP request.
 */
public interface HttpRequest extends HttpMessage, Request {

    @Override
    default boolean needReply() {
        return true;
    }

}

