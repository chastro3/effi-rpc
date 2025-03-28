package io.effi.rpc.protocol.http.support;

import io.effi.rpc.contract.Envelope;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;

/**
 * HTTP envelope that contains metadata and body data for an HTTP request or response.
 *
 * @param <BODY> the type of the body content for the HTTP message
 */
public interface HttpEnvelope<BODY> extends Envelope {

    /**
     * Returns the HTTP version of the current envelope.
     * This typically represents the version of the HTTP protocol (e.g., HTTP/1.1, HTTP/2).
     *
     * @return the {@link HttpVersion} of this envelope
     */
    HttpVersion version();

    /**
     * Returns the HTTP method of the current envelope.
     * This represents the HTTP request method (e.g., GET, POST) used in the HTTP message.
     *
     * @return the {@link HttpMethod} associated with this envelope
     */
    HttpMethod method();

    /**
     * Returns the HTTP headers of the current envelope.
     * These headers contain metadata for the HTTP request or response, such as content type, authorization, etc.
     *
     * @return the {@link HttpHeaders} of this envelope
     */
    HttpHeaders headers();

    /**
     * Returns the body data of the HTTP message.
     * The body represents the payload content of the request or response.
     *
     * @return the body of the envelope as a {@link BODY} type
     */
    BODY body();

    /**
     * Sets a new body for the HTTP message and returns a new {@link HttpEnvelope} instance with the updated body.
     *
     * @param body  the new body to set
     * @param <NEW> the type of the new body content
     * @return a new {@link HttpEnvelope} instance with the updated body of type {@link NEW}
     */
    <NEW> HttpEnvelope<NEW> body(NEW body);

    @Override
    default boolean isInstance() {
        return !(body() instanceof ByteBuf) && !(body() instanceof byte[]);
    }
}

