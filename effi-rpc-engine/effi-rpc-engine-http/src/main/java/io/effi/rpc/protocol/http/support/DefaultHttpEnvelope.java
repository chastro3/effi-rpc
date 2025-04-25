package io.effi.rpc.protocol.http.support;

import io.effi.rpc.config.URL;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Default implementation of {@link HttpEnvelope}.
 *
 * @param <BODY>
 */
public class DefaultHttpEnvelope<BODY> implements HttpEnvelope<BODY> {

    protected HttpVersion version;

    protected HttpMethod method;

    protected HttpHeaders headers;

    protected URL url;

    protected Object body;

    public DefaultHttpEnvelope() {

    }

    public DefaultHttpEnvelope(HttpVersion version, HttpMethod method, URL url, HttpHeaders headers, BODY body) {
        this.version = version;
        this.method = method;
        this.url = url;
        this.headers = headers;
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
    public <NEW> HttpEnvelope<NEW> body(NEW body) {
        this.body = body;
        HttpUtil.setContentLength(headers, body);
        return (HttpEnvelope<NEW>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BODY body() {
        return (BODY) body;
    }
}
