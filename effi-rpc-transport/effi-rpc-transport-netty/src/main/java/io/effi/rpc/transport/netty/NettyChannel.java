package io.effi.rpc.transport.netty;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty implementation of {@link io.effi.rpc.transport.endpoint.Channel}.
 */
public final class NettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final Channel channel;

    private NettyChannel(Channel channel, URL endpointUrl, EffiRpcModule module) {
        super(endpointUrl, module);
        this.channel = channel;
    }

    public static NettyChannel acquire(Channel channel, URL endpointUrl, EffiRpcModule module) {
        return CHANNEL_MAP.computeIfAbsent(channel, k -> new NettyChannel(channel, endpointUrl, module));
    }

    /**
     * Acquires a Channel instance from the given netty channel.
     *
     * @param channel
     * @return
     */
    public static NettyChannel acquire(Channel channel) {
        return CHANNEL_MAP.get(channel);
    }

    /**
     * Saves the given channel and endpointUrl into the channel map.
     *
     * @param channel
     * @param endpointUrl
     * @param module
     */
    public static void save(Channel channel, URL endpointUrl, EffiRpcModule module) {
        if (channel != null && endpointUrl != null) {
            acquire(channel, endpointUrl, module);
        }
    }

    public static void remove(Channel channel) {
        if (channel != null) {
            if (channel.isActive()) {
                channel.close();
            }
            CHANNEL_MAP.remove(channel);
        }
    }
    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public void close() {
        ChannelFuture channelFuture = channel.close();
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                if (CHANNEL_MAP.containsKey(channel) && CHANNEL_MAP.remove(channel, this)) {
                    clear();
                    logger.debug("{} closed", this);
                }
            } else {
                logger.error(this + " closure failed", future.cause());
            }
        });
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    protected void doSend(Object message) {
        channel.writeAndFlush(message);
    }

    public Channel channel() {
        return channel;
    }

}
