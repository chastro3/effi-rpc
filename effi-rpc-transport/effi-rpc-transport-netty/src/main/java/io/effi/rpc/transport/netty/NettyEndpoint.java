package io.effi.rpc.transport.netty;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.transport.endpoint.AbstractEndpoint;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

/**
 * Provides the base Netty endpoint with bootstrap configuration and channel initialization.
 */
public abstract class NettyEndpoint<B> extends AbstractEndpoint {

    protected ChannelConfigurer channelConfigurer;

    protected B bootstrap;

    protected NettyEndpoint(EndpointConfig config, InetSocketAddress address, ScopedPlatform platform, B bootstrap) {
        super(config, address, platform);
        this.bootstrap = AssertUtil.notNull(bootstrap, "bootstrap");
        initialize();
    }

    public void channelConfigurer(ChannelConfigurer channelConfigurer) {
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

    protected <V> void configureIfValid(OptionName<V> name, Consumer<V> consumer) {
        V value = config.option(name);
        if (value != null) consumer.accept(value);
    }

    protected boolean isActive(LazySingleton<Promise<NettyChannel>> future) {
        if (!future.initialized()) {
            return false;
        }
        Promise<NettyChannel> promise = future.ensure();
        if (!promise.completed()) {
            return false;
        }
        try {
            var result = promise.await();
            return result.succeeded() && result.value() != null && result.value().active();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    protected abstract void configureOptions(B bootstrap);

    protected abstract void configureChannelHandler(B bootstrap);

    private ChannelConfigurer ensureChannelConfigurer(ChannelConfigurer channelConfigurer) {
        return AssertUtil.notNull(channelConfigurer, "channel configurer");
    }
}
