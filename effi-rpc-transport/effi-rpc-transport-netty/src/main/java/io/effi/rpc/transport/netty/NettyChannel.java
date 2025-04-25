package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2StreamChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty implementation of {@link io.effi.rpc.transport.endpoint.Channel}.
 */
public final class NettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNELS = new ConcurrentHashMap<>();

    private final Channel channel;

    private NettyChannel(Channel channel, URL endpointUrl, EffiRpcModule module) {
        super(endpointUrl, module);
        this.channel = channel;
        registerCloseCallBack();
    }

    public static NettyChannel getOrCreate(Channel channel, URL endpointUrl, EffiRpcModule module) {
        AssertUtil.notNull(channel, "channel");
        return CHANNELS.computeIfAbsent(channel, k -> new NettyChannel(channel, endpointUrl, module));
    }

    public static NettyChannel get(Channel channel) {
        return CHANNELS.get(channel);
    }

    public static void remove(Channel channel) {
        if (channel != null) {
            if (channel.isActive()) {
                channel.close();
            }
            CHANNELS.remove(channel);
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
        channel.close();
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

    private void registerCloseCallBack() {
        channel.closeFuture().addListener(future -> {
                    if (future.isSuccess()) {
                        if (CHANNELS.containsKey(channel) && CHANNELS.remove(channel, this)) {
                            clear();
                            if (!(channel instanceof Http2StreamChannel))
                                logger.debug("channel {} closed", this);
                        }
                    } else {
                        logger.error(this + " closure failed", future.cause());
                    }
                }
        );
    }
}
