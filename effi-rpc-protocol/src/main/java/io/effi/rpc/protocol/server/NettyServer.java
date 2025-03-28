package io.effi.rpc.protocol.server;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.NettySupport;
import io.effi.rpc.protocol.client.Client;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.IOException;
import java.net.BindException;
import java.util.concurrent.ThreadFactory;

/**
 * Netty implementation of {@link Client}.
 */
public class NettyServer extends AbstractServer {

    protected Channel channel;

    private ServerBootstrap bootstrap;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    public NettyServer(InitializedConfig config) {
        super(config);
    }

    @Override
    protected void doInit() {
        int workThreads = url().getIntParam(DefaultConfigKeys.MAX_THREADS.key(), Constant.DEFAULT_MAX_CPU_THREADS);
        int maxUnConnections = url().getIntParam(DefaultConfigKeys.MAX_UN_CONNECTIONS.key(), Constant.DEFAULT_MAX_UN_CONNECTIONS);
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1, buildThreadFactory("server-boss"));
        workerGroup = new NioEventLoopGroup(workThreads, buildThreadFactory("server-worker"));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, maxUnConnections)
                //.option(ChannelOption.TCP_FASTOPEN_CONNECT, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(NettySupport.buildServerChannelInitializer(config, new ChannelManageHandler(this.activeChannels)));
    }

    @Override
    protected void doBind() throws BindException {
        ChannelFuture future = bootstrap.bind(port());
        future.syncUninterruptibly();
        if (!future.isSuccess()) {
            BindException bindException = new BindException();
            bindException.initCause(future.cause());
            throw bindException;
        }
        channel = future.channel();
    }

    @Override
    protected void doClose() throws IOException {
        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private ThreadFactory buildThreadFactory(String name) {
        String protocol = url().protocol();
        name = name + "-" + protocol;
        return new DefaultThreadFactory(name, false);
    }

}
