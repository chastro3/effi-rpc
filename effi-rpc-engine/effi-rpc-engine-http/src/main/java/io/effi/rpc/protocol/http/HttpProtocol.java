package io.effi.rpc.protocol.http;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.common.util.ObjectUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.engine.AbstractProtocol;
import io.effi.rpc.protocol.Protocol;
import io.effi.rpc.protocol.http.codec.HttpClientCodec;
import io.effi.rpc.protocol.http.codec.HttpServerCodec;
import io.effi.rpc.protocol.http.support.*;

import java.util.Map;

/**
 * Abstract Http implementation of {@link Protocol}.
 */
public abstract class HttpProtocol extends AbstractProtocol {

    private static final Map<CharSequence, CharSequence> REGULAR_REQUEST_HEADERS = HttpUtil.regularRequestHeaders();

    private static final Map<CharSequence, CharSequence> RESPONSE_REQUEST_HEADERS = HttpUtil.regularResponseHeaders();

    private final HttpVersion version;

    protected HttpProtocol(HttpVersion version) {
        super(version.protocolName(), new HttpServerCodec(), new HttpClientCodec());
        this.version = version;
    }

    @Override
    protected Envelope.Request createRequest(Caller<?> caller, URL requestUrl, Object body) {
        if (caller instanceof HttpCaller<?> httpCaller) {
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.add(REGULAR_REQUEST_HEADERS);
            HttpUtil.setContentType(headers, caller.config());
            return HttpRequest.builder()
                    .version(version)
                    .method(httpCaller.httpMethod())
                    .url(requestUrl)
                    .headers(headers)
                    .body(body)
                    .build();
        }
        throw new IllegalArgumentException(Messages.unSupport("caller", caller.getClass()));
    }

    @Override
    public Envelope.Response createResponse(Callee<?> callee, Result result) {
        if (callee instanceof HttpCallee<?> httpCallee) {
            int statusCode = 200;
            Object value = result.value();
            if (result.hasException()) {
                value = result.exception().getMessage();
                statusCode = 500;
            }
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.add(RESPONSE_REQUEST_HEADERS);
            HttpUtil.setContentType(headers, callee.config());
            return HttpResponse.builder()
                    .version(version)
                    .method(httpCallee.httpMethod())
                    .statusCode(statusCode)
                    .url(result.url())
                    .headers(headers)
                    .body(value)
                    .build();
        }
        throw new IllegalArgumentException("unsupported callee type :" + ObjectUtil.simpleClassName(callee));
    }

    @Override
    public Class<? extends Envelope.Request> supportedRequestType() {
        return HttpRequest.class;
    }

    @Override
    public Class<? extends Envelope.Response> supportedResponseType() {
        return HttpResponse.class;
    }

    /**
     * Returns http version.
     *
     * @return
     */
    public HttpVersion version() {
        return version;
    }

}
