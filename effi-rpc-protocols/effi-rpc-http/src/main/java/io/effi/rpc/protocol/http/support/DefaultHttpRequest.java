package io.effi.rpc.protocol.http.support;

import io.effi.rpc.config.URL;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides the default implementation of {@link HttpRequest}.
 */
public class DefaultHttpRequest<BODY> extends DefaultHttpMessage<BODY> implements HttpRequest<BODY> {

    public DefaultHttpRequest(HttpVersion version, HttpMethod method, URL url,
                              Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers, BODY body) {
        super(version, method, url, headers, body);
        HttpUtil.setContentLength(headers(), body);
    }

    @Override
    public <NEW> HttpRequest<NEW> body(NEW body) {
        return (HttpRequest<NEW>) super.body(body);
    }

    /**
     * Builds {@link DefaultHttpRequest} instance.
     */
    public static class Builder<BODY> extends DefaultHttpMessage.Builder<BODY, HttpRequest<BODY>, Builder<BODY>> {

        @Override
        public HttpRequest<BODY> build() {
            return new DefaultHttpRequest<>(version, method, url, headers, body);
        }
    }
}
