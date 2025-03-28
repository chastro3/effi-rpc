package io.effi.rpc.protocol.http.support;

import io.effi.rpc.common.url.URL;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Default implementation of {@link HttpRequest}.
 *
 * @param <BODY>
 */
public class DefaultHttpRequest<BODY> extends DefaultHttpEnvelope<BODY> implements HttpRequest<BODY> {

    public DefaultHttpRequest(HttpVersion version, HttpMethod method, URL url, HttpHeaders headers, BODY body) {
        super(version, method, url, headers, body);
        HttpUtil.setContentLength(headers, body);
    }

    @Override
    public <NEW> HttpRequest<NEW> body(NEW body) {
        return (HttpRequest<NEW>) super.body(body);
    }

}
