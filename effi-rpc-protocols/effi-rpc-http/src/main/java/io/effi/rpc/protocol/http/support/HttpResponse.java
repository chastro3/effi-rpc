package io.effi.rpc.protocol.http.support;

import io.effi.rpc.base.Envelope;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Represents an HTTP response.
 */
public interface HttpResponse<BODY> extends HttpEnvelope<BODY>, Envelope.Response {

    static <BODY> DefaultHttpResponse.Builder<BODY> builder() {
        return new DefaultHttpResponse.Builder<>();
    }

    /**
     * Returns the HTTP response status code.
     */
    int statusCode();

    @Override
    default String code() {
        return String.valueOf(statusCode());
    }

    @Override
    default String message() {
        return HttpResponseStatus.valueOf(statusCode()).reasonPhrase();
    }

    @Override
    default boolean isSuccess() {
        return statusCode() == HttpResponseStatus.OK.code();
    }

    @Override
    <NEW> HttpResponse<NEW> body(NEW body);
}

