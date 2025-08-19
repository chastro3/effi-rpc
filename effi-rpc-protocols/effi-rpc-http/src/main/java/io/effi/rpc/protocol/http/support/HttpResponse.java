package io.effi.rpc.protocol.http.support;

import io.effi.rpc.context.Response;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Represents an HTTP response.
 */
public interface HttpResponse extends HttpMessage, Response {

    /**
     * Returns the HTTP response status code.
     */
    int statusCode();

    @Override
    default boolean succeeded() {
        return statusCode() == HttpResponseStatus.OK.code();
    }
}

