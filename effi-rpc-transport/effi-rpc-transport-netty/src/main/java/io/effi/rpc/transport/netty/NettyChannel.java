package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.AbstractChannel;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2StreamChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Channel} using Netty.
 */
public final class NettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNELS = new ConcurrentHashMap<>();

    private final Channel channel;

    private NettyChannel(Channel channel, Endpoint endpoint, URL url) {
        super(endpoint, url);
        this.channel = channel;
        registerCloseCallBack();
    }

    public static NettyChannel init(Channel channel, Endpoint endpoint, URL url) {
        AssertUtil.notNull(channel, "channel");
        return CHANNELS.computeIfAbsent(channel, k -> new NettyChannel(channel, endpoint, url));
    }

    public static NettyChannel get(Channel channel) {
        return CHANNELS.get(channel);
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
    protected CompletableFuture<io.effi.rpc.transport.endpoint.Channel> doSend(Object message) {
        return NettySupport.wrap(channel.writeAndFlush(message), endpoint);
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
