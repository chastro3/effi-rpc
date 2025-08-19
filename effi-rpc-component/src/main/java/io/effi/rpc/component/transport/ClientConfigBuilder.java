package io.effi.rpc.component.transport;

import io.effi.rpc.config.ConfigNames;

/**
 * Builds {@link ClientConfig} instance and defines configuration.
 */
public interface ClientConfigBuilder<T extends ClientConfig, SELF extends ClientConfigBuilder<T, SELF>>
        extends EndpointConfigBuilder<T, SELF> {

    /**
     * Sets the connection timeout.
     */
    default SELF connectTimeout(int connectTimeout) {
        config().set(ConfigNames.CONNECT_TIMEOUT, connectTimeout);
        return self();
    }
}
