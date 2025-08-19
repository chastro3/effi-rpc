package io.effi.rpc.protocol.http;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.ConfigValues;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.context.Callee;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.context.support.AbstractCallee;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Provides a standard http implementation of {@link Callee}.
 */
public abstract class HttpCallee extends AbstractCallee {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders responseHeaders;

    @SuppressWarnings("rawtypes")
    protected HttpCallee(Builder builder) {
        super(builder);
        this.version = builder.version;
        String method = config.get(ConfigNames.HTTP_METHOD);
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
     * Builds {@link HttpCallee} instance and defines configuration.
     */
    public abstract static class Builder<T extends HttpCallee, SELF extends Builder<T, SELF>>
            extends AbstractCallee.Builder<T, SELF> {

        protected HttpVersion version;

        protected volatile HttpHeaders responseHeaders;

        protected Builder(HttpVersion version, MethodMapper<?> methodMapper, HierarchicalConfig config) {
            super(methodMapper, version.name(), config);
            this.version = AssertUtil.notNull(version, "version");
            if (StringUtil.isBlank(config.get(ConfigNames.SERIALIZATION))) {
                serialization(ConfigValues.Serialization.JSON);
            }
        }

        /**
         * Sets the HTTP method for the callee.
         */
        public SELF method(HttpMethod method) {
            config.set(ConfigNames.HTTP_METHOD, method.name());
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
