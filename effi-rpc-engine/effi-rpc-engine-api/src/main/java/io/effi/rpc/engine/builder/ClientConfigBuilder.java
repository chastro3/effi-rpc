package io.effi.rpc.engine.builder;

import io.effi.rpc.common.config.DefaultConfigKeys;
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
     */
    public C ssl(boolean ssl) {
        config.set(DefaultConfigKeys.SSL.key(), String.valueOf(ssl));
        return returnThis();
    }

    /**
     * Sets the maximum allowed connections.
     */
    public C maxConnections(int maxConnections) {
        config.set(DefaultConfigKeys.MAX_CONNECTIONS.key(), String.valueOf(maxConnections));
        return returnThis();
    }

    /**
     * Sets the maximum size for received messages.
     */
    public C maxMessageSize(int maxMessageSize) {
        config.set(DefaultConfigKeys.CLIENT_MAX_RECEIVE_SIZE.key(), String.valueOf(maxMessageSize));
        return returnThis();
    }

    /**
     * Sets the connection timeout.
     */
    public C connectTimeout(int connectTimeout) {
        config.set(DefaultConfigKeys.CONNECT_TIMEOUT.key(), String.valueOf(connectTimeout));
        return returnThis();
    }

    /**
     * Sets the idle count threshold for closing connections.
     */
    public C idleCountThreshold(int ideCountThreshold) {
        config.set(DefaultConfigKeys.IDLE_COUNT_THRESHOLD.key(), String.valueOf(ideCountThreshold));
        return returnThis();
    }

    /**
     * Sets the interval for triggering idle connections.
     */
    public C idleTriggerInterval(int idleTriggerInterval) {
        config.set(DefaultConfigKeys.IDLE_TRIGGER_INTERVAL.key(), String.valueOf(idleTriggerInterval));
        return returnThis();
    }

    /**
     * Enables or disables keep-alive.
     */
    public C keepAlive(boolean keepAlive) {
        config.set(DefaultConfigKeys.KEEP_ALIVE.key(), String.valueOf(keepAlive));
        return returnThis();
    }
}
