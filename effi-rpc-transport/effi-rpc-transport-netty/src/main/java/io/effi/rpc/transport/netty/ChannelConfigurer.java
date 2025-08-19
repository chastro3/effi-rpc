package io.effi.rpc.transport.netty;

import io.effi.rpc.component.transport.EndpointConfig;
import io.netty.channel.Channel;

/**
 * Configures netty {@link Channel} using the provided {@link EndpointConfig}.
 * <p>
 * Provides channel configuration functionality for applying endpoint
 * configurations to netty channels during initialization.
 */
public interface ChannelConfigurer {

    /**
     * Configures the given channel using the provided endpoint configuration.
     *
     * @param channel the channel to configure
     * @param config the configuration to apply
     */
    void configure(Channel channel, EndpointConfig config);
}
