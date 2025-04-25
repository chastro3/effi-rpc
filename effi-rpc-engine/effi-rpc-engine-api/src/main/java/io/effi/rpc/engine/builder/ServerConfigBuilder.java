package io.effi.rpc.engine.builder;

import io.effi.rpc.config.DefaultConfigKeys;
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
     */
    public C ssl(boolean ssl) {
        config.set(DefaultConfigKeys.SSL.key(), String.valueOf(ssl));
        return returnThis();
    }

    /**
     * Sets the maximum number of threads for the server.
     */
    public C maxThreads(int maxThreads) {
        config.set(DefaultConfigKeys.MAX_THREADS.key(), String.valueOf(maxThreads));
        return returnThis();
    }

    /**
     * Sets the maximum number of unconnected clients.
     */
    public C maxUnConnections(int maxUnConnections) {
        config.set(DefaultConfigKeys.MAX_UN_CONNECTIONS.key(), String.valueOf(maxUnConnections));
        return returnThis();
    }

    /**
     * Sets the maximum message size the server can receive.
     */
    public C maxMessageSize(int maxMessageSize) {
        config.set(DefaultConfigKeys.SERVER_MAX_RECEIVE_SIZE.key(), String.valueOf(maxMessageSize));
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
}


