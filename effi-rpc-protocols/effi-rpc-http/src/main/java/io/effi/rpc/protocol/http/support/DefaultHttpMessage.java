package io.effi.rpc.protocol.http.support;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.FluentBuilder;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides the default implementation of {@link HttpMessage}.
 */
public abstract class DefaultHttpMessage<BODY> implements HttpMessage<BODY> {

    protected HttpVersion version;

    protected HttpMethod method;

    protected HttpHeaders headers;

    protected URL url;

    protected Object body;

    protected DefaultHttpMessage(HttpVersion version, HttpMethod method, URL url,
                                 Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers, BODY body) {
        this.version = AssertUtil.notNull(version, "version");
        this.method = AssertUtil.notNull(method, "method");
        this.url = AssertUtil.notNull(url, "url");
        this.headers = version.createHeaders(headers);
        this.body = body;
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
    public URL url() {
        return url;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <NEW> HttpMessage<NEW> body(NEW body) {
        this.body = body;
        HttpUtil.setContentLength(headers, body);
        return (HttpMessage<NEW>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BODY body() {
        return (BODY) body;
    }

    /**
     * Builds {@link HttpMessage} instance and defines configuration.
     */
    public abstract static class Builder<BODY, T, C extends Builder<BODY, T, C>> implements FluentBuilder<T, C> {

        protected HttpVersion version;

        protected HttpMethod method;

        protected Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers;

        protected URL url;

        protected BODY body;

        public C version(HttpVersion version) {
            this.version = version;
            return returnThis();
        }

        public C method(HttpMethod method) {
            this.method = method;
            return returnThis();
        }

        public C url(URL url) {
            this.url = url;
            return returnThis();
        }

        public C headers(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers) {
            this.headers = headers;
            return returnThis();
        }

        public C body(BODY body) {
            this.body = body;
            return returnThis();
        }
    }
}
