package io.effi.rpc.protocol.http;

import io.effi.rpc.base.Callee;
import io.effi.rpc.boot.AbstractCallee;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Provides a standard http implementation of {@link Callee}.
 */
public abstract class HttpCallee extends AbstractCallee {

    protected HttpVersion version;

    protected HttpMethod httpMethod;

    protected HttpHeaders responseHeaders;

    protected HttpCallee(NodeConfig config, HttpCalleeBuilder<?, ?> builder) {
        super(config, builder);
        this.version = builder.version();
        String method = config.get(DefaultConfigNames.HTTP_METHOD);
        this.httpMethod = StringUtil.isNotBlank(method) ? HttpMethod.valueOf(method) : HttpMethod.POST;
        this.responseHeaders = builder.responseHeaders();
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
     * Returns the responseHeaders.
     */
    public HttpHeaders responseHeaders() {
        return responseHeaders;
    }
}
