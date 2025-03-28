package io.effi.rpc.protocol.http.support;

import io.effi.rpc.contract.Envelope;

/**
 * HTTP request.
 *
 * @param <BODY> the type of the body content of the HTTP request
 */
public interface HttpRequest<BODY> extends HttpEnvelope<BODY>, Envelope.Request {

    /**
     * Creates a new instance of {@link HttpRequestBuilder}, which is used to construct
     * an {@link HttpRequest} instance.
     *
     * @param <BODY> the type of the body content for the request being built
     * @return a new instance of {@link HttpRequestBuilder}
     */
    static <BODY> HttpRequestBuilder<BODY> builder() {
        return new HttpRequestBuilder<>();
    }

    @Override
    default boolean needReply() {
        return true;
    }

    @Override
    <NEW> HttpRequest<NEW> body(NEW body);
}
