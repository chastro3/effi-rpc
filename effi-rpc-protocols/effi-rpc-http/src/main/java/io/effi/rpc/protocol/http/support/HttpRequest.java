package io.effi.rpc.protocol.http.support;

import io.effi.rpc.base.Message;

/**
 * Represents an HTTP request.
 */
public interface HttpRequest<BODY> extends HttpMessage<BODY>, Message.Request {

    static <BODY> DefaultHttpRequest.Builder<BODY> builder() {
        return new DefaultHttpRequest.Builder<>();
    }

    @Override
    default boolean needReply() {
        return true;
    }

    @Override
    <NEW> HttpRequest<NEW> body(NEW body);
}

