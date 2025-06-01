package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.Channel;

/**
 * Configures the channel for {@link Endpoint}.
 */
public abstract class ChannelConfigurer<E extends Endpoint> {

    protected E endpoint;

    protected ChannelConfigurer(E endpoint) {
        this.endpoint = AssertUtil.notNull(endpoint, "endpoint");
    }

    /**
     * Configures the channel to connect with the given endpoint using the provided URL.
     * <p>
     * It is recommended to use the {@code url} parameter for configuration, not
     * {@link Endpoint#url()}. For example, when HTTP2 is compatible with HTTP1.1,
     * {@link Endpoint#url()} contains HTTP2-specific configuration, whereas the
     * {@code url} parameter provides HTTP1.1 configuration details.
     *
     * @param channel  the channel to be configured
     * @param endpoint the target endpoint
     * @param url      the URL with HTTP1.1 connection details
     */
    public void configure(Channel channel, URL url) {
        initChannel(channel, url);
        doConfigure(channel, url);
    }

    protected abstract void doConfigure(Channel channel, URL url);

    protected void initChannel(Channel channel, URL url) {
        NettyChannel.init(channel, endpoint, url);
    }
}



