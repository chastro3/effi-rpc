package io.effi.rpc.protocol.http.support;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.FluentBuilder;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides the default implementation of {@link HttpMessage}.
 */
public abstract class StandardHttpMessage implements HttpMessage {

    protected HttpVersion version;

    protected HttpMethod method;

    protected HttpHeaders headers;

    protected SmartURL url;

    protected Object body;

    protected StandardHttpMessage(Builder<?, ?> builder) {
        this.version = AssertUtil.notNull(builder.version, "version");
        this.method = AssertUtil.notNull(builder.method, "method");
        this.url = AssertUtil.notNull(builder.url, "url");
        this.headers = version.newHeaders(builder.headers);
        this.body = builder.body;
    }

    public StandardHttpMessage withBody(Object body) {
        this.body = body;
        return this;
    }

    @Override
    public HttpVersion version() {
        return version;
    }

    @Override
    public HttpMethod method() {
        return method;
    }

    @Override
    public SmartURL url() {
        return url;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T body() {
        return (T) body;
    }

    /**
     * Builds {@link HttpMessage} instance and defines configuration.
     */
    public abstract static class Builder<T, SELF extends Builder<T, SELF>> implements FluentBuilder<T, SELF> {

        protected HttpVersion version;

        protected HttpMethod method;

        protected Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers;

        protected SmartURL url;

        protected Object body;

        public SELF version(HttpVersion version) {
            this.version = version;
            return self();
        }

        public SELF method(HttpMethod method) {
            this.method = method;
            return self();
        }

        public SELF url(SmartURL url) {
            this.url = url;
            return self();
        }

        public SELF headers(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
            this.headers = headers;
            return self();
        }

        public SELF body(Object body) {
            this.body = body;
            return self();
        }
    }
}
