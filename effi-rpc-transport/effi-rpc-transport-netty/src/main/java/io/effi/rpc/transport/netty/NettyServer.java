package io.effi.rpc.transport.netty;

import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.ChannelTracker;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Implements {@link Server} using Netty.
 * <p>
 * Provides Netty-based server implementation with channel management,
 * connection handling, and protocol support.
 */
public class NettyServer extends NettyEndpoint<ServerBootstrap> implements Server, ChannelTracker {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    protected NioEventLoopGroup bossGroup;

    protected NioEventLoopGroup workerGroup;

    protected final LazySingleton<Promise<NettyChannel>> serverChannelFuture = LazySingleton.from(
            () -> NettyChannel.wrapWhenActive(bootstrap.bind(), this)
    );

    protected Map<ChannelId, Channel> activeChannels = new ConcurrentHashMap<>();

    public NettyServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform, new ServerBootstrap());
    }

    @Override
    public Promise<NettyChannel> bind() {
        return serverChannelFuture.ensure();
    }

    @Override
    public boolean isActive() {
        return isActive(serverChannelFuture);
    }

    @Override
    public void close() {
        if (isActive()) {
            for (Channel channel : activeChannels.values()) {
                try {
                    channel.close();
                } catch (Throwable e) {
                    EffiRpcException failed = PredefinedErrorCode.CLOSE_CHANNEL
                            .fail(e, channel.remoteAddress());
                    logger.error(failed.getMessage(), e);
                }
            }
            serverChannelFuture.ensure().result().close();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
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
        int bossThreads = config.getConfig(ConfigNames.CONNECTION_HANDLER_THREADS);
        int workThreads = config.getConfig(ConfigNames.REQUEST_PROCESSOR_THREADS);
        bossGroup = new NioEventLoopGroup(bossThreads, newThreadFactory("server-boss"));
        workerGroup = new NioEventLoopGroup(workThreads, newThreadFactory("server-worker"));
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(localAddress())
                .channel(NioServerSocketChannel.class);
        configureIfValid(ConfigNames.ACCEPT_BACKLOG, val -> {
            bootstrap.option(ChannelOption.SO_BACKLOG, val);
        });
        configureIfValid(ConfigNames.SEND_BUFFER_SIZE, val -> {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, val);
        });
        configureIfValid(ConfigNames.RECEIVE_BUFFER_SIZE, val -> {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, val);
        });
        configureIfValid(ConfigNames.TCP_NO_DELAY, val -> {
            bootstrap.childOption(ChannelOption.TCP_NODELAY, val);
        });
        configureIfValid(ConfigNames.TCP_KEEP_ALIVE, val -> {
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
