package io.effi.rpc.transport.netty;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.ConfigName;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.transport.endpoint.AbstractEndpoint;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.util.AssertUtil;
import io.netty.handler.ssl.SslContext;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides an abstract implementation of netty {@link Endpoint}.
 */
public abstract class AbstractNettyEndpoint<E extends Endpoint, B> extends AbstractEndpoint {

    protected NettyChannel channel;

    protected ChannelConfigurer<E> channelConfigurer;

    protected B bootstrap;

    // Sharable handler
    protected SslContext sslContext;

    protected HeartBeatHandler heartBeatHandler;

    protected AbstractNettyEndpoint(EndpointConfig config, InetSocketAddress address, EffiRpcPlatform platform, B bootstrap) {
        super(config, address, platform);
        this.bootstrap = AssertUtil.notNull(bootstrap, "bootstrap");
        initialize();
    }

    @SuppressWarnings("unchecked")
    public void channelConfigurer(ChannelConfigurer<?> channelConfigurer) {
        this.channelConfigurer = (ChannelConfigurer<E>) channelConfigurer;
    }

    public void sslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public SslContext sslContext() {
        return sslContext;
    }

    public HeartBeatHandler heartBeatHandler() {
        return heartBeatHandler;
    }

    protected void configureChannel(io.netty.channel.Channel channel) {
        AssertUtil.notNull(channelConfigurer, "channelConfigurer");
        channelConfigurer.configure(channel, url);
    }

    protected abstract void configureOptions(B bootstrap);

    protected abstract void configureChannelHandler(B bootstrap);

    protected void initialize() {
        initialBootStrap();
    }

    protected void initialBootStrap() {
        this.heartBeatHandler = new HeartBeatHandler(this);
        configureOptions(bootstrap);
        configureChannelHandler(bootstrap);
    }

    protected <V> void configureIfValid(ConfigName key, Function<String, V> converter, Consumer<V> consumer) {
        String value = url.getParam(key);
        try {
            V val = (value != null) ? converter.apply(value) : null;
            if (val != null) {
                consumer.accept(val);
            }
        } catch (Exception ignore) {
        }
    }
}
