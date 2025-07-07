package io.effi.rpc.protocol.http;

import io.effi.rpc.boot.builder.CallerBuilder;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeToken;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Builds {@link HttpCaller} instance and defines configuration.
 */
public abstract class HttpCallerBuilder<T extends HttpCaller<?>, C extends HttpCallerBuilder<T, C>>
        extends CallerBuilder<T, C> {

    protected HttpVersion version;

    protected volatile HttpHeaders requestHeaders;

    protected HttpCallerBuilder(HttpVersion version, TypeToken<?> returnType, NodeConfig config) {
        super(returnType, config);
        this.version = AssertUtil.notNull(version, "version");
        if (StringUtil.isBlank(config.get(DefaultConfigNames.SERIALIZATION))) {
            serialization(Component.Serialization.JSON);
        }
    }

    /**
     * Sets the HTTP method for the callee.
     *
     * @param method The HTTP method to set (e.g., GET, POST).
     * @return This builder instance for fluent chaining.
     */
    public C method(HttpMethod method) {
        config.set(DefaultConfigNames.HTTP_METHOD.realName(), method.name());
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

    @Override
    public String protocol() {
        return version().protocolName();
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
                    requestHeaders = version().createHeaders();
                }
            }
        }
        return requestHeaders;
    }

}
