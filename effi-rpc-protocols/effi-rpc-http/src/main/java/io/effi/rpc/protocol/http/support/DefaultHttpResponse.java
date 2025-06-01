package io.effi.rpc.protocol.http.support;

import io.effi.rpc.config.URL;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Provides the default implementation of {@link HttpResponse}.
 */
public class DefaultHttpResponse<BODY> extends DefaultHttpEnvelope<BODY> implements HttpResponse<BODY> {

    private final int statusCode;

    public DefaultHttpResponse(HttpVersion version, HttpMethod method, URL url, int statusCode,
                               Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers, BODY body) {
        super(version, method, url, headers, body);
        this.statusCode = statusCode;
        HttpUtil.setContentLength(headers(), body);
    }

    @Override
    public <NEW> HttpResponse<NEW> body(NEW body) {
        return (HttpResponse<NEW>) super.body(body);
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    /**
     * Builds {@link DefaultHttpResponse} instance.
     */
    public static class Builder<BODY> extends DefaultHttpEnvelope.Builder<BODY, HttpResponse<BODY>, Builder<BODY>> {

        private int statusCode;

        /**
         * Sets the status code for the HTTP response.
         */
        public DefaultHttpResponse.Builder<BODY> statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @Override
        public HttpResponse<BODY> build() {
            return new DefaultHttpResponse<>(version, method, url, statusCode, headers, body);
        }
    }
}
