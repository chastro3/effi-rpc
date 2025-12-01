package io.effi.rpc.protocol.http;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.support.AbstractCaller;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Provides a standard http implementation of {@link Caller}.
 */
public abstract class HttpCaller<R> extends AbstractCaller<R> {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders requestHeaders;

    @SuppressWarnings("rawtypes")
    protected HttpCaller(Builder builder) {
        super(builder);
        this.version = builder.version;
        String method = option(HttpProtocol.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.requestHeaders = builder.requestHeaders;
    }

    /**
     * Returns the version.
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Returns the httpMethod.
     */
    public HttpMethod httpMethod() {
        return httpMethod;
    }

    /**
     * Returns the requestHeaders.
     */
    public HttpHeaders requestHeaders() {
        return requestHeaders;
    }

    /**
     * Builds {@link HttpCaller} instance and defines configuration.
     */
    public abstract static class Builder<T extends HttpCaller<?>, C extends Builder<T, C>>
            extends AbstractCaller.Builder<T, C> {

        protected HttpVersion version;

        protected volatile HttpHeaders requestHeaders;

        protected Builder(HttpVersion version, TypeCapture<?> returnType) {
            super(returnType, version.name());
            this.version = AssertUtil.notNull(version, "version");
            if (StringUtil.isBlank(option(Peer.SERIALIZER))) {
                serializer(JacksonSerializer.NAME);
            }
        }

        /**
         * Sets the HTTP method for the callee.
         *
         * @param method the HTTP method to set (e.g., GET, POST).
         * @return This builder instance for fluent chaining.
         */
        public C method(HttpMethod method) {
            addOption(HttpProtocol.HTTP_METHOD, method.name());
            return self();
        }

        /**
         * Adds a single request header to the caller.
         *
         * @param key   the header key.
         * @param value the header value.
         */
        public C addRequestHeader(CharSequence key, CharSequence value) {
            if (!StringUtil.isBlank(key) && !StringUtil.isBlank(value)) {
                delayedRequestHeaders().add(key, value);
            }
            return self();
        }

        protected HttpHeaders delayedRequestHeaders() {
            if (requestHeaders == null) {
                synchronized (this) {
                    if (requestHeaders == null) {
                        requestHeaders = version.newHeaders();
                    }
                }
            }
            return requestHeaders;
        }
    }
}
