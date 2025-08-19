package io.effi.rpc.transport.netty;

import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.transport.endpoint.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;

import java.net.InetSocketAddress;

/**
 * Implements of {@link Client} using Netty with fixed channel pools for connection reuse.
 * <p>
 * Provides Netty-based client implementation with connection pooling
 * for efficient resource management and connection reuse.
 */
public class NettyPoolClient extends NettyClient {

    protected FixedChannelPool channelPool;

    public NettyPoolClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        super(config, remoteAddress, platform);
    }

    @Override
    protected void configureChannelHandler(Bootstrap bootstrap) {
        int maxConnections = config().getConfig(ConfigNames.MAX_CONNECTIONS);
        this.channelPool = new FixedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                configureChannel(ch);
            }
        }, maxConnections);
    }

    @Override
    public Promise<NettyChannel> fetchChannel() {
        return NettyChannel.wrap(channelPool.acquire());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void close() {
        channelPool.close();
    }

    public void release(Channel channel) {
        channelPool.release(channel);
    }
}

