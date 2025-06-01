package io.effi.rpc.protocol.http;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.base.Caller;
import io.effi.rpc.boot.AbstractCaller;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Provides a standard http implementation of {@link Caller}.
 */
public abstract class HttpCaller<R> extends AbstractCaller<R> {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders requestHeaders;

    protected HttpCaller(NodeConfig config, HttpCallerBuilder<?, ?> builder) {
        super(config, builder);
        this.version = builder.version();
        String method = config.get(DefaultConfigKeys.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.requestHeaders = builder.requestHeaders();
    }

    /**
     * Returns the version.
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Returns the httpMethod.
     */
    public HttpMethod httpMethod() {
        return httpMethod;
    }

    /**
     * Returns the requestHeaders.
     */
    public HttpHeaders requestHeaders() {
        return requestHeaders;
    }
}
