package io.effi.rpc.protocol;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.extension.AbstractAttributes;
import io.effi.rpc.common.extension.resoruce.Closeable;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLSource;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty's {@link Channel} wrapper.
 */
public final class NettyChannel extends AbstractAttributes implements URLSource, ModuleSource, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private final URL endpointUrl;

    private final EffiRpcModule module;

    private final Channel channel;

    private NettyChannel(Channel channel, URL endpointUrl, EffiRpcModule module) {
        this.endpointUrl = endpointUrl;
        this.module = module;
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

    /**
     * Send the given message to the remote peer.
     *
     * @param message
     */
    public void send(Object message) {
        if (isActive()) {
            channel.writeAndFlush(message);
        }
    }

    @Override
    public void close() {
        try {
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
        } catch (Throwable e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.CLOSE, e);
        }
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    /**
     * Returns the underlying netty channel.
     *
     * @return
     */
    public Channel channel() {
        return channel;
    }

    @Override
    public URL url() {
        return endpointUrl;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public String toString() {
        return channel.toString();
    }
}
