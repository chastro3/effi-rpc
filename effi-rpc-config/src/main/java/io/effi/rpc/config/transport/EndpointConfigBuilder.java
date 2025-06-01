package io.effi.rpc.config.transport;

import io.effi.rpc.config.ConfigSource;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.util.FluentBuilder;

/**
 * Builds {@link EndpointConfig} instance and defines configuration.
 */
public interface EndpointConfigBuilder<T extends EndpointConfig, C extends EndpointConfigBuilder<T, C>>
        extends ConfigSource, FluentBuilder<T, C> {

    /**
     * Set the send buffer size.
     */
    default C sendBufferSize(int sendBufferSize) {
        config().set(DefaultConfigKeys.SEND_BUFFER_SIZE, sendBufferSize);
        return returnThis();
    }

    /**
     * Set the receive buffer size.
     */
    default C receiveBufferSize(int receiveBufferSize) {
        config().set(DefaultConfigKeys.RECEIVE_BUFFER_SIZE, receiveBufferSize);
        return returnThis();
    }

    /**
     * Set traffic shaping configuration for the server.
     * <p>
     * Traffic shaping helps to control and limit the rate of traffic to avoid resource exhaustion.
     */
    C trafficShapingOptions(TrafficShapingConfig trafficShapingConfig);
}


