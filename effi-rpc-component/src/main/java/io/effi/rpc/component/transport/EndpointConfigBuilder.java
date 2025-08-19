package io.effi.rpc.component.transport;

import io.effi.rpc.config.ConfigBuilder;
import io.effi.rpc.config.ConfigNames;

/**
 * Builds {@link EndpointConfig} instance and defines configuration.
 */
public interface EndpointConfigBuilder<T extends EndpointConfig, SELF extends EndpointConfigBuilder<T, SELF>>
        extends ConfigBuilder<T, SELF> {

    /**
     * Set the send buffer size.
     */
    default SELF sendBufferSize(int sendBufferSize) {
        config().set(ConfigNames.SEND_BUFFER_SIZE, sendBufferSize);
        return self();
    }

    /**
     * Set the receive buffer size.
     */
    default SELF receiveBufferSize(int receiveBufferSize) {
        config().set(ConfigNames.RECEIVE_BUFFER_SIZE, receiveBufferSize);
        return self();
    }
}


