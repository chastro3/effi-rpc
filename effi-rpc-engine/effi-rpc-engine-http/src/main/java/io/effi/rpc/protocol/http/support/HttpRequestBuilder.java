package io.effi.rpc.protocol.http.support;

/**
 * Build an HTTP request.
 *
 * @param <BODY> The type of the body of the HTTP request.
 */
public class HttpRequestBuilder<BODY> extends HttpEnvelopeBuilder<BODY, HttpRequest<BODY>, HttpRequestBuilder<BODY>> {

    @Override
    public <NEW> HttpRequestBuilder<NEW> body(NEW body) {
        return (HttpRequestBuilder<NEW>) super.body(body);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpRequest<BODY> build() {
        return (HttpRequest<BODY>) new DefaultHttpRequest<>(version, method, url, headers, body);
    }
}

