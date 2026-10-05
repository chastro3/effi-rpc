package io.effi.rpc.transport.netty;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.options.ClientOptions;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.transport.endpoint.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.FixedChannelPool;

import java.net.InetSocketAddress;

/**
 * Implements {@link Client} using a fixed Netty channel pool for connection reuse.
 */
public class NettyPoolClient extends NettyClient {

    protected FixedChannelPool channelPool;

    public NettyPoolClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        super(config, remoteAddress, platform);
    }

    @Override
    public Future<NettyChannel> fetchChannel() {
        return NettyChannel.wrap(channelPool.acquire());
    }

    @Override
    public boolean active() {
        return true;
    }

    @Override
    public void close() {
        channelPool.close();
    }

    @Override
    public void release(io.effi.rpc.transport.endpoint.Channel channel) {
        if (channel instanceof NettyChannel nettyChannel && nettyChannel.physical()) {
            channelPool.release(nettyChannel.channel());
            return;
        }
        channel.close();
    }

    @Override
    public void discard(io.effi.rpc.transport.endpoint.Channel channel) {
        if (channel instanceof NettyChannel nettyChannel && nettyChannel.physical()) {
            Channel raw = nettyChannel.channel();
            raw.close().addListener(ignored -> channelPool.release(raw));
            return;
        }
        channel.close();
    }

    /**
     * Returns the current pool usage snapshot.
     */
    public PoolMetrics poolMetrics() {
        return new PoolMetrics(
                channelPool.acquiredChannelCount(),
                config().option(ClientOptions.MAX_CONNECTIONS)
        );
    }

    @Override
    protected void configureChannelHandler(Bootstrap bootstrap) {
        int maxConnections = config().option(ClientOptions.MAX_CONNECTIONS);
        int maxPendingAcquires = config().option(ClientOptions.MAX_PENDING_ACQUIRES);
        int acquireTimeout = config().option(ClientOptions.ACQUIRE_TIMEOUT);
        this.channelPool = new FixedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                configureChannel(ch);
            }
        }, ChannelHealthChecker.ACTIVE, FixedChannelPool.AcquireTimeoutAction.FAIL,
                acquireTimeout, maxConnections, maxPendingAcquires);
    }

    public record PoolMetrics(int acquired, int maxConnections) {
    }
}

