package io.effi.rpc.transport.netty;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

/**
 * Implements {@link Server} using Netty.
 */
public class NettyServer extends AbstractNettyEndpoint<NettyServer, ServerBootstrap> implements Server {

    private final Object lock = new Object();

    protected NioEventLoopGroup bossGroup;

    protected NioEventLoopGroup workerGroup;

    protected volatile CompletableFuture<Void> boundFuture;

    protected ChannelManageHandler channelManager;

    public NettyServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform, new ServerBootstrap());
    }

    @Override
    public CompletableFuture<Void> bind() {
        if (boundFuture == null) {
            synchronized (lock) {
                if (boundFuture == null) {
                    ChannelFuture future = bootstrap.bind();
                    boundFuture = new CompletableFuture<>();
                    future.addListener(v -> {
                        if (future.isSuccess()) {
                            boundFuture.complete(null);
                        } else {
                            boundFuture.completeExceptionally(future.cause());
                        }
                    });
                }
            }
        }
        return boundFuture;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void close() {
        for (Channel channel : channels()) {
            try {
                channel.close();
            } catch (Throwable e) {
                throw PredefinedErrorCode.CLOSE_CHANNEL.fail(e, channel.remoteAddress());
            }
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public Channel lookupChannel(InetSocketAddress remoteAddress) {
        for (Channel channel : channels()) {
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
    public Collection<Channel> channels() {
        return channelManager.activeChannels();
    }

    public ChannelManageHandler channelManager() {
        return channelManager;
    }

    @Override
    protected void initialBootStrap() {
        this.channelManager = new ChannelManageHandler(this);
        super.initialBootStrap();
    }

    @Override
    protected void configureOptions(ServerBootstrap bootstrap) {
        URL url = url();
        int bossThreads = url.getIntParam(DefaultConfigKeys.CONNECTION_HANDLER_THREADS);
        int workThreads = url.getIntParam(DefaultConfigKeys.REQUEST_PROCESSOR_THREADS);
        bossGroup = new NioEventLoopGroup(bossThreads, newThreadFactory("server-boss"));
        workerGroup = new NioEventLoopGroup(workThreads, newThreadFactory("server-worker"));
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(host(), port())
                .channel(NioServerSocketChannel.class);
        configureIfValid(DefaultConfigKeys.ACCEPT_BACKLOG, Integer::parseInt, val -> {
            bootstrap.option(ChannelOption.SO_BACKLOG, val);
        });
        configureIfValid(DefaultConfigKeys.SEND_BUFFER_SIZE, Integer::parseInt, val -> {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, val);
        });
        configureIfValid(DefaultConfigKeys.RECEIVE_BUFFER_SIZE, Integer::parseInt, val -> {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, val);
        });
        configureIfValid(DefaultConfigKeys.NO_DELAY, Boolean::parseBoolean, val -> {
            bootstrap.childOption(ChannelOption.TCP_NODELAY, val);
        });
        configureIfValid(DefaultConfigKeys.KEEP_ALIVE, Boolean::parseBoolean, val -> {
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, val);
        });
    }

    @Override
    protected void configureChannelHandler(ServerBootstrap bootstrap) {
        bootstrap.childHandler(NettySupport.newChannelInitializer(this::configureChannel));
    }

    private ThreadFactory newThreadFactory(String name) {
        String protocol = url().protocol();
        name = name + "-(" + protocol + ")";
        return new DefaultThreadFactory(name, false);
    }

}
