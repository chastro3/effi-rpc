package io.effi.rpc.protocol.http;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.engine.builder.CallerBuilder;
import io.effi.rpc.protocol.http.support.DefaultHttpHeaders;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

/**
 * Builder for creating {@link HttpCaller} instances,defining settings for callee.
 *
 * @param <T> The type of {@link HttpCaller}.
 * @param <C> The type of the builder.
 */
public abstract class HttpCallerBuilder<T extends HttpCaller<?>, C extends HttpCallerBuilder<T, C>>
        extends CallerBuilder<T, C> {

    protected HttpVersion version = HttpVersion.HTTP_1_1;

    protected volatile HttpHeaders requestHeaders;

    protected HttpCallerBuilder(TypeToken<?> returnType, LinkedConfig config) {
        super(returnType, config);
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
        config.set(DefaultConfigKeys.HTTP_METHOD.key(), method.name());
        return returnThis();
    }

    /**
     * Adds multiple request headers to the callee.
     *
     * @param headers The headers to be added.
     */
    public C addRequestHeaders(Map<CharSequence, CharSequence> headers) {
        if (CollectionUtil.isNotEmpty(headers)) {
            delayedRequestHeaders().add(headers);
        }
        return returnThis();
    }

    /**
     * Adds a single request header to the caller.
     *
     * @param key   The header key.
     * @param value The header value.
     */
    public C addRequestHeader(CharSequence key, CharSequence value) {
        if (!StringUtil.isBlank(key) && !StringUtil.isBlank(value)) {
            delayedRequestHeaders().add(key, value);
        }
        return returnThis();
    }

    /**
     * Returns the version.
     */
    public HttpVersion version() {
        return version;
    }

    /**
     * Returns the requestHeaders.
     */
    public HttpHeaders requestHeaders() {
        return requestHeaders;
    }

    /**
     * Lazily initializes the request headers if they are not already set.
     * This ensures that the headers are only created when needed, improving performance.
     *
     * @return The HttpHeaders object that holds the request headers.
     */
    protected HttpHeaders delayedRequestHeaders() {
        if (requestHeaders == null) {
            synchronized (this) {
                if (requestHeaders == null) {
                    requestHeaders = new DefaultHttpHeaders();
                }
            }
        }
        return requestHeaders;
    }

}
