package io.effi.rpc.protocol.http.support;

import io.effi.rpc.base.Envelope;

/**
 * Represents an HTTP request.
 */
public interface HttpRequest<BODY> extends HttpEnvelope<BODY>, Envelope.Request {

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

