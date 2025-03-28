package io.effi.rpc.protocol.http.support;

/**
 * Build an HTTP response.
 *
 * @param <BODY> The type of the body of the HTTP response.
 */
public class HttpResponseBuilder<BODY> extends HttpEnvelopeBuilder<BODY, HttpResponse<BODY>, HttpResponseBuilder<BODY>> {

    private int statusCode;

    /**
     * Sets the status code for the HTTP response.
     *
     * @param statusCode The status code to set for the response.
     * @return The current instance of HttpResponseBuilder for method chaining.
     */
    public HttpResponseBuilder<BODY> statusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public <NEW> HttpResponseBuilder<NEW> body(NEW body) {
        return (HttpResponseBuilder<NEW>) super.body(body);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpResponse<BODY> build() {
        return (HttpResponse<BODY>) new DefaultHttpResponse<>(version, method, url, statusCode, headers, body);
    }
}

