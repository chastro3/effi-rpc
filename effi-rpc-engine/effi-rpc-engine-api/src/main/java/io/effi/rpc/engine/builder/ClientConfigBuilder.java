package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.contract.config.ClientConfig;

/**
 * Builder for creating {@link ClientConfig} instances,defining settings for client.
 *
 * @param <T> The type of {@link ClientConfig}.
 * @param <C> The type of the builder.
 */
public abstract class ClientConfigBuilder<T extends ClientConfig, C extends ClientConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> {

    /**
     * Enables or disables SSL.
     *
     * @param ssl True to enable SSL
     * @return This builder
     */
    public C ssl(boolean ssl) {
        config.set(DefaultConfigKeys.SSL.key(), String.valueOf(ssl));
        return returnThis();
    }

    /**
     * Sets the maximum allowed connections.
     *
     * @param maxConnections Maximum number of connections
     * @return This builder
     */
    public C maxConnections(int maxConnections) {
        config.set(DefaultConfigKeys.MAX_CONNECTIONS.key(), String.valueOf(maxConnections));
        return returnThis();
    }

    /**
     * Sets the maximum size for received messages.
     *
     * @param maxMessageSize Maximum message size
     * @return This builder
     */
    public C maxMessageSize(int maxMessageSize) {
        config.set(DefaultConfigKeys.CLIENT_MAX_RECEIVE_SIZE.key(), String.valueOf(maxMessageSize));
        return returnThis();
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectTimeout Timeout in milliseconds
     * @return This builder
     */
    public C connectTimeout(int connectTimeout) {
        config.set(DefaultConfigKeys.CONNECT_TIMEOUT.key(), String.valueOf(connectTimeout));
        return returnThis();
    }

    /**
     * Sets the timeout for keep-alive connections.
     *
     * @param keepAliveTimeout Keep-alive timeout in milliseconds
     * @return This builder
     */
    public C keepAliveTimeout(int keepAliveTimeout) {
        config.set(DefaultConfigKeys.KEEP_ALIVE_TIMEOUT.key(), String.valueOf(keepAliveTimeout));
        return returnThis();
    }

    /**
     * Sets the idle times before closing spare connections.
     *
     * @param spareCloseTimes Number of idle times
     * @return This builder
     */
    public C spareCloseTimes(int spareCloseTimes) {
        config.set(DefaultConfigKeys.SPARE_CLOSE_TIMES.key(), String.valueOf(spareCloseTimes));
        return returnThis();
    }

    /**
     * Enables or disables keep-alive.
     *
     * @param keepAlive True to enable keep-alive
     * @return This builder
     */
    public C keepAlive(boolean keepAlive) {
        config.set(DefaultConfigKeys.KEEP_ALIVE.key(), String.valueOf(keepAlive));
        return returnThis();
    }
}
