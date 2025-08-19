package io.effi.rpc.transport.netty;

import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.LazySingleton;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Implements {@link Client} using Netty.
 * <p>
 * Provides Netty-based client implementation with channel management,
 * connection handling, and protocol support.
 */
public class NettyClient extends NettyEndpoint<Bootstrap> implements Client {

    public static final GenericKey<NioEventLoopGroup> EVENT_LOOP_GROUP_KEY = GenericKey.valueOf("nio-event-loop-group");

    protected final LazySingleton<Promise<NettyChannel>> channelFuture = LazySingleton.from(
            () -> NettyChannel.wrapWhenActive(bootstrap.connect(), this)
    );

    public NettyClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        super(config, remoteAddress, platform, new Bootstrap());
    }

    @Override
    public Future<NettyChannel> fetchChannel() {
        return channelFuture.ensure();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return address;
    }

    @Override
    public boolean isActive() {
        return isActive(channelFuture);
    }

    @Override
    public void close() {
        if (isActive()) fetchChannel().result().close();
    }

    @Override
    public ClientConfig config() {
        return (ClientConfig) config;
    }

    protected void configureOptions(Bootstrap bootstrap) {
        int connectTimeout = config.getConfig(ConfigNames.CONNECT_TIMEOUT);
        NioEventLoopGroup platformEventLoopGroup = platform.externalComponent(EVENT_LOOP_GROUP_KEY);
        bootstrap.group(platformEventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(remoteAddress())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        configureIfValid(ConfigNames.SEND_BUFFER_SIZE, val -> {
            bootstrap.option(ChannelOption.SO_SNDBUF, val);
        });
        configureIfValid(ConfigNames.RECEIVE_BUFFER_SIZE, val -> {
            bootstrap.option(ChannelOption.SO_RCVBUF, val);
        });
        configureIfValid(ConfigNames.TCP_NO_DELAY, val -> {
            bootstrap.option(ChannelOption.TCP_NODELAY, val);
        });
        configureIfValid(ConfigNames.TCP_KEEP_ALIVE, val -> {
            bootstrap.option(ChannelOption.SO_KEEPALIVE, val);
        });
    }

    @Override
    protected void configureChannelHandler(Bootstrap bootstrap) {
        bootstrap.handler(NettySupport.newChannelInitializer(this::configureChannel));
    }

}

