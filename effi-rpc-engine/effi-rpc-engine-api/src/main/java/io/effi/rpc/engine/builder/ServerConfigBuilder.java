package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.contract.config.ServerConfig;

/**
 * Builder for configuring {@link ServerConfig} instances.
 *
 * @param <T> The type of {@link ServerConfig}.
 * @param <C> The type of the builder.
 */
public abstract class ServerConfigBuilder<T extends ServerConfig, C extends ServerConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> {

    /**
     * Enables or disables SSL for the server.
     *
     * @param ssl Whether SSL is enabled
     * @return This builder
     */
    public C ssl(boolean ssl) {
        config.set(DefaultConfigKeys.SSL.key(), String.valueOf(ssl));
        return returnThis();
    }

    /**
     * Sets the maximum number of threads for the server.
     *
     * @param maxThreads Maximum number of threads
     * @return This builder
     */
    public C maxThreads(int maxThreads) {
        config.set(DefaultConfigKeys.MAX_THREADS.key(), String.valueOf(maxThreads));
        return returnThis();
    }

    /**
     * Sets the maximum number of unconnected clients.
     *
     * @param maxUnConnections Maximum number of unconnected clients
     * @return This builder
     */
    public C maxUnConnections(int maxUnConnections) {
        config.set(DefaultConfigKeys.MAX_UN_CONNECTIONS.key(), String.valueOf(maxUnConnections));
        return returnThis();
    }

    /**
     * Sets the maximum message size the server can receive.
     *
     * @param maxMessageSize Maximum message size
     * @return This builder
     */
    public C maxMessageSize(int maxMessageSize) {
        config.set(DefaultConfigKeys.SERVER_MAX_RECEIVE_SIZE.key(), String.valueOf(maxMessageSize));
        return returnThis();
    }

    /**
     * Sets the keep-alive timeout for the server.
     *
     * @param keepAliveTimeout Keep-alive timeout in milliseconds
     * @return This builder
     */
    public C keepAliveTimeout(int keepAliveTimeout) {
        config.set(DefaultConfigKeys.KEEP_ALIVE_TIMEOUT.key(), String.valueOf(keepAliveTimeout));
        return returnThis();
    }
}


