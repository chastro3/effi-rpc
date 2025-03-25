package io.effi.rpc.protocol.http;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.support.AbstractCallee;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Standard Abstract Http Callee.
 *
 * @param <T>
 */
public abstract class HttpCallee<T> extends AbstractCallee<T> {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders responseHeaders;

    protected HttpCallee(Config config, HttpCalleeBuilder<?, ?> builder) {
        super(config, builder);
        this.version = builder.version();
        String method = config.get(DefaultConfigKeys.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.responseHeaders = builder.responseHeaders();
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
     * Returns the version.
     *
     * @return the version
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Returns the httpMethod.
     *
     * @return the httpMethod
     */
    public HttpMethod httpMethod() {
        return httpMethod;
    }

    /**
     * Returns the responseHeaders.
     *
     * @return the responseHeaders
     */
    public HttpHeaders responseHeaders() {
        return responseHeaders;
    }
}
