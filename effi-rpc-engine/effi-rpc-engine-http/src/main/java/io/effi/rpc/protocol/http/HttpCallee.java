package io.effi.rpc.protocol.http;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.engine.AbstractCallee;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
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

    protected HttpCallee(NodeConfig config, HttpCalleeBuilder<?, ?> builder) {
        super(config, builder);
        this.version = builder.version();
        String method = config.get(DefaultConfigKeys.HTTP_METHOD);
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
