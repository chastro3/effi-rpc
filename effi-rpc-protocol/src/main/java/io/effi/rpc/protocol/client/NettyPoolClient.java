package io.effi.rpc.protocol.client;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.NettyChannel;
import io.effi.rpc.protocol.NettySupport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Netty-based client that utilizes a fixed channel pool for efficient connection management.
 * <p>
 * This implementation allows for the reuse of channels, reducing the overhead of creating and
 * destroying connections for each request. It uses a {@link FixedChannelPool} to manage
 * the channels, ensuring that a maximum number of connections is maintained.
 * </p>
 */
public class NettyPoolClient extends NettyClient {

    protected FixedChannelPool channelPool;

    public NettyPoolClient(InitializedConfig config) {
        super(config);
    }

    @Override
    protected void configHandler(Bootstrap bootstrap) {
        // Acquire a ChannelPoolHandler for managing channels in the pool
        // Set up the fixed channel pool with a maximum number of connections
        int maxConnections = url().getIntParam(DefaultConfigKeys.MAX_UN_CONNECTIONS.key(), Constant.DEFAULT_CLIENT_MAX_CONNECTIONS);
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
    public NettyChannel acquireChannel() {
        Future<Channel> future = channelPool.acquire();
        boolean success = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
        // Check the outcome of acquiring a channel
        if (success && future.isSuccess()) {
            return NettyChannel.acquire(future.getNow(), url(), module());
        }
        Throwable cause = future.cause();
        throw EffiRpcException.wrap(
                PredefinedErrorCode.ACQUIRE_CHANNEL,
                cause == null
                        ? new TimeoutException("Connect to " + url().address() + " timeout")
                        : cause,
                url().address(),
                url().protocol()
        );
    }

    @Override
    public boolean isActive() {
        // Return the initialization status of the client
        return isInit;
    }

    @Override
    public void close() {
        // Close the channel pool, releasing all resources
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

