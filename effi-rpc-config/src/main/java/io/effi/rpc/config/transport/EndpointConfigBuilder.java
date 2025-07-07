package io.effi.rpc.config.transport;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.util.FluentBuilder;

/**
 * Builds {@link EndpointConfig} instance and defines configuration.
 */
public interface EndpointConfigBuilder<T extends EndpointConfig, C extends EndpointConfigBuilder<T, C>>
        extends Config.Provider, FluentBuilder<T, C> {

    /**
     * Set the send buffer size.
     */
    default C sendBufferSize(int sendBufferSize) {
        config().set(DefaultConfigNames.SEND_BUFFER_SIZE, sendBufferSize);
        return returnThis();
    }

    /**
     * Set the receive buffer size.
     */
    default C receiveBufferSize(int receiveBufferSize) {
        config().set(DefaultConfigNames.RECEIVE_BUFFER_SIZE, receiveBufferSize);
        return returnThis();
    }

    /**
     * Set traffic shaping configuration for the server.
     * <p>
     * Traffic shaping helps to control and limit the rate of traffic to avoid resource exhaustion.
     */
    C trafficShapingOptions(TrafficShapingConfig trafficShapingConfig);
}


