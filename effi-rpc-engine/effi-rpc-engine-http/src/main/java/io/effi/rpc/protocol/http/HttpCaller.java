package io.effi.rpc.protocol.http;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.engine.AbstractCaller;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Standard Http Caller implementation of {@link Caller}.
 *
 * @param <R>
 */
public abstract class HttpCaller<R> extends AbstractCaller<R> {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders requestHeaders;

    protected HttpCaller(Config config, HttpCallerBuilder<?, ?> builder) {
        super(config, builder);
        this.version = builder.version();
        String method = config.get(DefaultConfigKeys.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.requestHeaders = builder.requestHeaders();
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
     * Returns the requestHeaders.
     *
     * @return the requestHeaders
     */
    public HttpHeaders requestHeaders() {
        return requestHeaders;
    }
}
