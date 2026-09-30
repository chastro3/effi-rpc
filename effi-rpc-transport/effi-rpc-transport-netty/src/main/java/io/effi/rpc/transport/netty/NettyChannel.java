package io.effi.rpc.transport.netty;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.AbstractChannel;
import io.effi.rpc.transport.endpoint.ChannelTracker;
import io.effi.rpc.transport.endpoint.Endpoint;
import io.effi.rpc.transport.ChannelCallBindings;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ExceptionUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Channel} using Netty.
 * <p>
 * Provides Netty-based channel implementation with connection management,
 * message sending, and channel tracking functionality.
 */
public final class NettyChannel extends AbstractChannel {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);

    private static final ConcurrentMap<Channel, NettyChannel> CHANNELS = new ConcurrentHashMap<>();

    private final Channel channel;

    private final boolean virtual;

    private NettyChannel(Channel channel, Endpoint endpoint, EndpointConfig config, boolean virtual) {
        super(endpoint, config);
        this.channel = channel;
        this.virtual = virtual;
        track();
    }

    public static NettyChannel init(Channel channel, Endpoint endpoint, EndpointConfig config, boolean virtual) {
        AssertUtil.notNull(channel, "channel");
        return CHANNELS.computeIfAbsent(channel, k -> new NettyChannel(channel, endpoint, config, virtual));
    }

    public static NettyChannel ensure(Channel channel) {
        AssertUtil.notNull(channel, "channel");
        NettyChannel nettyChannel = CHANNELS.get(channel);
        if (nettyChannel != null) return nettyChannel;
        throw new IllegalStateException("Netty channel is unexpectedly null."
                + "Please ensure that the current endpoint has properly initialized its Netty channel.");
    }

    public static Promise<NettyChannel> wrap(ChannelFuture future) {
        return wrap(future, future::channel, NettyChannel::ensure);
    }

    public static Promise<NettyChannel> wrap(io.netty.util.concurrent.Future<? extends Channel> future) {
        return wrap(future, future::getNow, NettyChannel::ensure);
    }

    public static Promise<NettyChannel> wrapWhenActive(ChannelFuture future, Endpoint endpoint) {
        return wrap(future, future::channel, ch -> init(ch, endpoint, endpoint.config(), false));
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
        ChannelFuture closeFuture = channel.close();
        if (!channel.eventLoop().inEventLoop()) {
            closeFuture.awaitUninterruptibly();
        }
    }

    @Override
    public boolean active() {
        return channel.isActive();
    }

    @Override
    public String toString() {
        return StringUtil.format(
                "{}[local={}, remote={}, active={}, type={}]",
                ObjectUtil.simpleClassName(this),
                localAddress(), remoteAddress(), active(),
                physical() ? "physical" : "virtual"
        );
    }


    @Override
    protected Future<Void> doSend(Object message) {
        if (!channel.isWritable()) {
            Promise<Void> promise = new Promise<>();
            promise.failure(TransportErrorCodes.CHANNEL_OVERLOADED.fail(this));
            return promise;
        }
        return Futures.asVoid(wrap(channel.writeAndFlush(message)));
    }

    public Channel channel() {
        return channel;
    }

    public boolean virtual() {
        return virtual;
    }

    public boolean physical() {
        return !virtual;
    }

    public NettyChannel parent() {
        Channel parent = channel.parent();
        if (parent == null) return null;
        return ensure(parent);
    }

    private static <T extends io.netty.util.concurrent.Future<?>> Promise<NettyChannel>
    wrap(T future, java.util.function.Supplier<Channel> channelSupplier, Function<Channel, NettyChannel> wrapper) {
        Promise<NettyChannel> promise = new Promise<>();
        promise.onCancel(reason -> future.cancel(false));
        future.addListener(result -> {
            if (result.isSuccess()) {
                Channel channel = channelSupplier.get();
                try {
                    NettyChannel nettyChannel = wrapper.apply(channel);
                    if (promise.completed()) {
                        nettyChannel.close();
                        return;
                    }
                    promise.success(nettyChannel);
                } catch (Exception e) {
                    promise.failure(TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                            e, "unknown", ExceptionUtil.message(e)));
                }
            } else {
                promise.failure(TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                        result.cause(), "unknown", ExceptionUtil.message(result.cause())));
            }
        });
        return promise;
    }


    private void track() {
        maybeTrackChannel();
        channel.closeFuture().addListener(this::handleClose);
    }

    private void handleClose(io.netty.util.concurrent.Future<? super Void> future) {
        if (future.isSuccess()) {
            maybeUnTrackChannel();
            maybeCancelCalls();
            if (CHANNELS.remove(channel, this)) {
                clear();
                if (physical()) {
                    logger.debug("Channel '{}' closed", this);
                }
            }
        } else {
            logger.error("Closure of '{}' failed", future.cause(), this);
        }
    }


    private void maybeTrackChannel() {
        if (physical()) {
            ChannelTracker channelTracker = findChannelTracker();
            if (channelTracker != null) channelTracker.add(this);
        }
    }

    private void maybeUnTrackChannel() {
        if (physical()) {
            ChannelTracker channelTracker = findChannelTracker();
            if (channelTracker != null) channelTracker.remove(this);
        }
    }

    private void maybeCancelCalls() {
        ChannelCallBindings bindings = endpoint.platform().singleComponent(ChannelCallBindings.class);
        if (bindings != null) {
            bindings.cancelChannel(this, TransportErrorCodes.CHANNEL_INACTIVE.fail(this));
        }
    }

    private ChannelTracker findChannelTracker() {
        if (endpoint instanceof ChannelTracker channelTracker) {
            return channelTracker;
        } else if (endpoint instanceof ChannelTracker.Supplier supplier) {
            return supplier.channelTracker();
        }
        return null;
    }

}
