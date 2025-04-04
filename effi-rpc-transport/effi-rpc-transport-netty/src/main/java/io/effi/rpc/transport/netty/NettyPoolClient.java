package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.transport.endpoint.Client;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Netty implementation of {@link Client} with a fixed channel pool for efficient connection reuse.
 */
public class NettyPoolClient extends NettyClient {

    protected FixedChannelPool channelPool;

    public NettyPoolClient(NettyEndpointConfig config) {
        super(config);
    }

    @Override
    protected void configHandler() {
        // Acquire a ChannelPoolHandler for managing channels in the pool
        // Set up the fixed channel pool with a maximum number of connections
        int maxConnections = url().getIntParam(DefaultConfigKeys.MAX_CONNECTIONS.key(), Constant.DEFAULT_CLIENT_MAX_CONNECTIONS);
        channelPool = new FixedChannelPool(bootstrap, buildChannelPoolHandler(), maxConnections);
    }

    protected ChannelPoolHandler buildChannelPoolHandler() {
        return NettySupport.buildChannelPoolHandler(config);
    }

    @Override
    protected void doConnect() throws ConnectException {
        // Connection is handled by the channel pool; no direct connection is made here.
    }

    @Override
    public io.effi.rpc.transport.endpoint.Channel acquireChannel() {
        Future<Channel> future = channelPool.acquire();
        boolean success = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
        // Check the outcome of acquiring a channel
        if (success && future.isSuccess()) {
            return NettyChannel.acquire(future.getNow(), url(), module());
        }
        Throwable cause = future.cause();
        cause = cause != null ? cause : new TimeoutException("Connect to " + url().address() + " timeout");
        throw PredefinedErrorCode.ACQUIRE_CHANNEL.fail(cause, url().address(), url().protocol());
    }

    @Override
    public boolean isActive() {
        return isInit;
    }

    @Override
    public void close() {
        channelPool.close();
    }

    /**
     * Releases the specified channel back to the channel pool.
     *
     * @param channel The channel to be released.
     */
    public void release(Channel channel) {
        channelPool.release(channel);
    }
}

