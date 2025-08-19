package io.effi.rpc.protocol.http.support;

import io.effi.rpc.exception.EffiRpcException;

/**
 * Provides the default implementation of {@link HttpResponse}.
 */
public class HttpDuplexResponse extends HttpIOMessage<HttpDuplexResponse> implements HttpResponse {

    private final int statusCode;

    public HttpDuplexResponse(Builder builder) {
        super(builder);
        this.statusCode = builder.statusCode;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public EffiRpcException cause() {
        return body instanceof EffiRpcException ? (EffiRpcException) body : null;
    }

    /**
     * Builds {@link HttpDuplexResponse} instance.
     */
    public static class Builder extends StandardHttpMessage.Builder<HttpDuplexResponse, Builder> {

        private int statusCode;

        /**
         * Sets the status code for the HTTP response.
         */
        public HttpDuplexResponse.Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public HttpDuplexResponse build() {
            return new HttpDuplexResponse(this);
        }
    }
}
