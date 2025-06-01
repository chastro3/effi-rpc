package io.effi.rpc.protocol.http;

import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.boot.builder.CalleeBuilder;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Builds {@link HttpCallee} instance and defines configuration.
 */
public abstract class HttpCalleeBuilder<T extends HttpCallee<?>, C extends HttpCalleeBuilder<T, C>>
        extends CalleeBuilder<T, C> {

    protected HttpVersion version;

    protected volatile HttpHeaders responseHeaders;

    protected HttpCalleeBuilder(HttpVersion version, MethodMapper<?> methodMapper, NodeConfig config) {
        super(methodMapper, config);
        this.version = AssertUtil.notNull(version, "version");
        if (StringUtil.isBlank(config.get(DefaultConfigKeys.SERIALIZATION))) {
            serialization(Component.Serialization.JSON);
        }
    }

    /**
     * Sets the HTTP method for the callee.
     */
    public C method(HttpMethod method) {
        config.set(DefaultConfigKeys.HTTP_METHOD, method.name());
        return returnThis();
    }

    /**
     * Adds a single response header to the callee.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public C addResponseHeader(CharSequence key, CharSequence value) {
        if (!StringUtil.isBlank(key) && !StringUtil.isBlank(value)) {
            delayedResponseHeaders().add(key, value);
        }
        return returnThis();
    }

    public HttpVersion version() {
        return version;
    }

    public HttpHeaders responseHeaders() {
        return responseHeaders;
    }

    @Override
    public String protocol() {
        return version().protocolName();
    }

    protected HttpHeaders delayedResponseHeaders() {
        if (responseHeaders == null) {
            synchronized (this) {
                if (responseHeaders == null) {
                    responseHeaders = version().createHeaders();
                }
            }
        }
        return responseHeaders;
    }
}
