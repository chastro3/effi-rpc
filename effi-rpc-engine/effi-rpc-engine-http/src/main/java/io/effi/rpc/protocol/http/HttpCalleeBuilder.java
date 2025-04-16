package io.effi.rpc.protocol.http;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.engine.builder.CalleeBuilder;
import io.effi.rpc.protocol.http.support.DefaultHttpHeaders;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Builder for creating {@link HttpCallee} instances,defining settings for callee.
 *
 * @param <T> The type of {@link HttpCallee}.
 * @param <C> The type of the builder.
 */
public abstract class HttpCalleeBuilder<T extends HttpCallee<?>, C extends HttpCalleeBuilder<T, C>>
        extends CalleeBuilder<T, C> {

    protected HttpVersion version = HttpVersion.HTTP_1_1;

    protected volatile HttpHeaders responseHeaders;

    protected HttpCalleeBuilder(MethodMapper<?> methodMapper, NodeConfig config) {
        super(methodMapper, config);
        if (StringUtil.isBlank(config.get(DefaultConfigKeys.SERIALIZATION))) {
            serialization(Component.Serialization.JSON);
        }
    }

    /**
     * Sets the HTTP version for the callee.
     *
     * @param version The HTTP version to set (e.g., HTTP/1.1, HTTP/2).
     * @return This builder instance for fluent chaining.
     */
    public C version(HttpVersion version) {
        this.version = version;
        return returnThis();
    }

    /**
     * Sets the HTTP method for the callee.
     *
     * @param method The HTTP method to set (e.g., GET, POST).
     * @return This builder instance for fluent chaining.
     */
    public C method(HttpMethod method) {
        config.set(DefaultConfigKeys.HTTP_METHOD, method.name());
        return returnThis();
    }

    /**
     * Adds multiple response headers to the callee.
     *
     * @param headers The headers to be added.
     */
    public void addResponseHeaders(Map<CharSequence, CharSequence> headers) {
        if (CollectionUtil.isNotEmpty(headers)) {
            delayedResponseHeaders().add(headers);
        }
    }

    /**
     * Adds a single response header to the callee.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public void addResponseHeader(CharSequence key, CharSequence value) {
        if (!StringUtil.isBlank(key) && !StringUtil.isBlank(value)) {
            delayedResponseHeaders().add(key, value);
        }
    }

    /**
     * Returns the version.
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Returns the responseHeaders.
     */
    public HttpHeaders responseHeaders() {
        return responseHeaders;
    }

    /**
     * Lazily initializes the response headers if they are not already set.
     * This ensures that the headers are only created when needed, improving performance.
     *
     * @return The HttpHeaders object that holds the response headers.
     */
    protected HttpHeaders delayedResponseHeaders() {
        if (responseHeaders == null) {
            synchronized (this) {
                if (responseHeaders == null) {
                    responseHeaders = new DefaultHttpHeaders();
                }
            }
        }
        return responseHeaders;
    }
}
