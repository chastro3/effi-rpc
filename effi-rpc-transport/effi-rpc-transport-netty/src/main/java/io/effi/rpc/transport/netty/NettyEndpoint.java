package io.effi.rpc.transport.netty;

import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.config.ConfigName;
import io.effi.rpc.transport.endpoint.AbstractEndpoint;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Provides an abstract implementation of netty endpoints.
 * <p>
 * Provides base functionality for netty-based endpoints with bootstrap
 * configuration, channel handling, and initialization support.
 */
public abstract class NettyEndpoint<B> extends AbstractEndpoint {

    protected ChannelConfigurer channelConfigurer;

    protected B bootstrap;

    protected NettyEndpoint(EndpointConfig config, InetSocketAddress address, ScopedPlatform platform, B bootstrap) {
        super(config, address, platform);
        this.bootstrap = AssertUtil.notNull(bootstrap, "bootstrap");
        initialize();
    }

    public void withChannelConfigurer(ChannelConfigurer channelConfigurer) {
        this.channelConfigurer = ensureChannelConfigurer(channelConfigurer);
    }

    protected void configureChannel(io.netty.channel.Channel channel) {
        ensureChannelConfigurer(channelConfigurer).configure(channel, config);
    }

    protected void initialize() {
        configureBootStrap();
    }

    protected void configureBootStrap() {
        configureOptions(bootstrap);
        configureChannelHandler(bootstrap);
    }

    protected <V> void configureIfValid(ConfigName<V> name, Consumer<V> consumer) {
        V value = config.getConfig(name);
        if (value != null) consumer.accept(value);
    }

    protected boolean isActive(LazySingleton<Promise<NettyChannel>> future) {
        return future.initialized() && future.ensure().succeeded();
    }

    private ChannelConfigurer ensureChannelConfigurer(ChannelConfigurer channelConfigurer) {
        return AssertUtil.notNull(channelConfigurer, "channel configurer");
    }

    protected abstract void configureOptions(B bootstrap);

    protected abstract void configureChannelHandler(B bootstrap);
}
