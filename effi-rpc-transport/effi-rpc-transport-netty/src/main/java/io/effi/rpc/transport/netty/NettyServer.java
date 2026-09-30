package io.effi.rpc.transport.netty;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.component.transport.options.ServerOptions;
import io.effi.rpc.component.transport.options.TcpOptions;
import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.ChannelTracker;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements {@link Server} using Netty.
 * <p>
 * Provides Netty-based server implementation with channel management,
 * connection handling, and protocol support.
 */
public class NettyServer extends NettyEndpoint<ServerBootstrap> implements Server, ChannelTracker {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 5L;

    protected NioEventLoopGroup bossGroup;

    protected NioEventLoopGroup workerGroup;

    protected final LazySingleton<Promise<NettyChannel>> serverChannelFuture = LazySingleton.from(
            () -> NettyChannel.wrapWhenActive(bootstrap.bind(), this)
    );

    protected Map<ChannelId, Channel> activeChannels = new ConcurrentHashMap<>();

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public NettyServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform, new ServerBootstrap());
    }

    @Override
    public Promise<NettyChannel> bind() {
        return serverChannelFuture.ensure();
    }

    @Override
    public boolean active() {
        return isActive(serverChannelFuture);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        for (Channel channel : activeChannels.values()) {
            try {
                channel.close();
            } catch (Throwable e) {
                EffiRpcException fail = TransportErrorCodes.CLOSE_CHANNEL
                        .fail(e, channel.remoteAddress());
                logger.error(fail.getMessage(), e);
            }
        }
        activeChannels.clear();
        if (serverChannelFuture.initialized()) {
            Promise<NettyChannel> bindResult = serverChannelFuture.ensure();
            try {
                var result = bindResult.await();
                if (result.succeeded() && result.value() != null) {
                    result.value().close();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Future<?> bossShutdown = shutdownGracefully(bossGroup);
        Future<?> workerShutdown = shutdownGracefully(workerGroup);
        awaitShutdown(bossShutdown, "server-boss");
        awaitShutdown(workerShutdown, "server-worker");

    }

    private Future<?> shutdownGracefully(EventLoopGroup group) {
        if (group == null) {
            return null;
        }
        return group.shutdownGracefully(0, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void awaitShutdown(Future<?> shutdown, String name) {
        if (shutdown == null) {
            return;
        }
        try {
            if (!shutdown.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Timed out waiting for {} event loop to stop", name);
                shutdown.cancel(false);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            shutdown.cancel(false);
        }
    }

    @Override
    public Channel lookupChannel(InetSocketAddress remoteAddress) {
        for (Channel channel : activeChannels.values()) {
            if (NetUtil.isSameAddress(channel.remoteAddress(), remoteAddress)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public ServerConfig config() {
        return (ServerConfig) config;
    }

    @Override
    public InetSocketAddress localAddress() {
        return address;
    }

    @Override
    public Collection<Channel> channels() {
        return list();
    }

    @Override
    public void add(Channel channel) {
        activeChannels.put(((NettyChannel) channel).channel().id(), channel);
    }

    @Override
    public void remove(Channel channel) {
        activeChannels.remove(((NettyChannel) channel).channel().id());
    }

    @Override
    public Collection<Channel> list() {
        return Collections.unmodifiableCollection(activeChannels.values());
    }

    @Override
    public int size() {
        return activeChannels.size();
    }

    @Override
    protected void configureOptions(ServerBootstrap bootstrap) {
        int bossThreads = config.option(ServerOptions.ACCEPTOR_THREADS);
        int workThreads = config.option(ServerOptions.IO_THREADS);
        bossGroup = new NioEventLoopGroup(bossThreads, newThreadFactory("server-boss"));
        workerGroup = new NioEventLoopGroup(workThreads, newThreadFactory("server-worker"));
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(localAddress())
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, NettySupport.newWriteBufferWaterMark(config));
        configureIfValid(ServerOptions.ACCEPT_BACKLOG, val -> {
            bootstrap.option(ChannelOption.SO_BACKLOG, val);
        });
        configureIfValid(TransportOptions.SEND_BUFFER_SIZE, val -> {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, val);
        });
        configureIfValid(TransportOptions.RECEIVE_BUFFER_SIZE, val -> {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, val);
        });
        configureIfValid(TcpOptions.NO_DELAY, val -> {
            bootstrap.childOption(ChannelOption.TCP_NODELAY, val);
        });
        configureIfValid(TcpOptions.KEEP_ALIVE, val -> {
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, val);
        });
    }

    @Override
    protected void configureChannelHandler(ServerBootstrap bootstrap) {
        bootstrap.childHandler(NettySupport.newChannelInitializer(this::configureChannel));
    }

    private ThreadFactory newThreadFactory(String name) {
        String protocol = protocol().name();
        name = name + "-(" + protocol + ")";
        return new DefaultThreadFactory(name, false);
    }
}
