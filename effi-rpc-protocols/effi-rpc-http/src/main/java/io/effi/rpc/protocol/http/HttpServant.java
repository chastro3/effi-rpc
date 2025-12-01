package io.effi.rpc.protocol.http;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.context.support.AbstractServant;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Provides a standard http implementation of {@link Servant}.
 */
public abstract class HttpServant extends AbstractServant {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders responseHeaders;

    @SuppressWarnings("rawtypes")
    protected HttpServant(Builder builder) {
        super(builder);
        this.version = builder.version;
        String method = option(HttpProtocol.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.responseHeaders = builder.responseHeaders;
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
     * Returns the responseHeaders.
     */
    public HttpHeaders responseHeaders() {
        return responseHeaders;
    }

    /**
     * Builds {@link HttpServant} instance and defines configuration.
     */
    public abstract static class Builder<T extends HttpServant, SELF extends Builder<T, SELF>>
            extends AbstractServant.Builder<T, SELF> {

        protected HttpVersion version;

        protected volatile HttpHeaders responseHeaders;

        protected Builder(HttpVersion version, ServantMethod<?> servantMethod) {
            super(servantMethod, version.name());
            this.version = AssertUtil.notNull(version, "version");
            // TODO 优化配置
            if (StringUtil.isBlank(option(Peer.SERIALIZER))) {
                serializer(JacksonSerializer.NAME);
            }
        }

        /**
         * Sets the HTTP method for the callee.
         */
        public SELF method(HttpMethod method) {
            addOption(HttpProtocol.HTTP_METHOD, method.name());
            return self();
        }

        /**
         * Adds a single response header to the callee.
         *
         * @param key   the header key.
         * @param value the header value.
         */
        public SELF addResponseHeader(CharSequence key, CharSequence value) {
            if (!StringUtil.isBlank(key) && !StringUtil.isBlank(value)) {
                delayedResponseHeaders().add(key, value);
            }
            return self();
        }


        protected HttpHeaders delayedResponseHeaders() {
            if (responseHeaders == null) {
                synchronized (this) {
                    if (responseHeaders == null) {
                        responseHeaders = version.newHeaders();
                    }
                }
            }
            return responseHeaders;
        }
    }
}
