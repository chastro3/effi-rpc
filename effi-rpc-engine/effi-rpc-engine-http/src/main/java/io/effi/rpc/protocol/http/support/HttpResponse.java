package io.effi.rpc.protocol.http.support;

import io.effi.rpc.contract.Envelope;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * HTTP response.
 *
 * @param <BODY> the type of the body content of the HTTP response
 */
public interface HttpResponse<BODY> extends HttpEnvelope<BODY>, Envelope.Response {

    /**
     * Creates a new instance of {@link HttpResponseBuilder}, which is used to construct
     * an {@link HttpResponse} instance.
     *
     * @param <BODY> the type of the body content for the response being built
     * @return a new instance of {@link HttpResponseBuilder}
     */
    static <BODY> HttpResponseBuilder<BODY> builder() {
        return new HttpResponseBuilder<>();
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

