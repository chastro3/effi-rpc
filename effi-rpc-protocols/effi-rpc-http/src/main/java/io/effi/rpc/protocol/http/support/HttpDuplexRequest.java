package io.effi.rpc.protocol.http.support;

/**
 * Provides the default implementation of {@link HttpRequest}.
 */
public class HttpDuplexRequest extends HttpIOMessage<HttpDuplexRequest> implements HttpRequest {

    HttpDuplexRequest(Builder builder) {
        super(builder);
    }

    public static Builder builder() {
        return new Builder();
    }


    /**
     * Builds {@link HttpDuplexRequest} instance.
     */
    public static class Builder extends StandardHttpMessage.Builder<HttpDuplexRequest, Builder> {

        @Override
        public HttpDuplexRequest build() {
            return new HttpDuplexRequest(this);
        }
    }
}
