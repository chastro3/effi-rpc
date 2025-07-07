package io.effi.rpc.transport.netty;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.transport.endpoint.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Implements of {@link Client} using Netty, with a fixed channel pool for efficient connection reuse.
 */
public class NettyPoolClient extends NettyClient {

    protected FixedChannelPool channelPool;

    public NettyPoolClient(ClientConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void configureChannelHandler(Bootstrap bootstrap) {
        int maxConnections = url().getIntParam(DefaultConfigNames.MAX_CONNECTIONS);
        channelPool = new FixedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                configureChannel(ch);
            }
        }, maxConnections);
    }

    @Override
    public CompletableFuture<io.effi.rpc.transport.endpoint.Channel> getChannel() {
        return NettySupport.wrap(channelPool.acquire(), this);
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

