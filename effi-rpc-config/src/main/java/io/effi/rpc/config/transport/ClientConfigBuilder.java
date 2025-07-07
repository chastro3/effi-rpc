package io.effi.rpc.config.transport;

import io.effi.rpc.config.DefaultConfigNames;

/**
 * Builds {@link ClientConfig} instance and defines configuration.
 */
public interface ClientConfigBuilder<T extends ClientConfig, C extends ClientConfigBuilder<T, C>>
        extends EndpointConfigBuilder<T, C> {

    /**
     * Sets the connection timeout.
     */
    default C connectTimeout(int connectTimeout) {
        config().set(DefaultConfigNames.CONNECT_TIMEOUT, connectTimeout);
        return returnThis();
    }
}
