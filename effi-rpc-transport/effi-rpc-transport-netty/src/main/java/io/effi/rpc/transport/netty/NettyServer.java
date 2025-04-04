package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.transport.endpoint.AbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * Netty implementation of {@link io.effi.rpc.transport.endpoint.Server}.
 */
public class NettyServer extends AbstractServer {

    protected NettyEndpointConfig config;

    protected Channel channel;

    private ServerBootstrap bootstrap;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    public NettyServer(NettyEndpointConfig config) {
        super(config.url(), config.module());
        this.config = AssertUtil.notNull(config, "config");
        bind();
    }

    @Override
    protected void doInit() {
        int workThreads = url().getIntParam(DefaultConfigKeys.MAX_THREADS);
        int maxUnConnections = url().getIntParam(DefaultConfigKeys.MAX_UN_CONNECTIONS);
        ChannelManageHandler channelManageHandler = new ChannelManageHandler(this.activeChannels, this);
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1, buildThreadFactory("server-boss"));
        workerGroup = new NioEventLoopGroup(workThreads, buildThreadFactory("server-worker"));
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, maxUnConnections)
                //.option(ChannelOption.TCP_FASTOPEN_CONNECT, true)
                .childOption(ChannelOption.SO_KEEPALIVE, url().getBooleanParam(DefaultConfigKeys.KEEP_ALIVE))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(NettySupport.buildServerChannelInitializer(config, channelManageHandler));
    }

    @Override
    protected void doBind() throws Throwable {
        ChannelFuture future = bootstrap.bind(port()).syncUninterruptibly();
        if (!future.isSuccess()) {
            throw future.cause();
        }
        channel = future.channel();

    }

    @Override
    protected void doClose() throws Throwable {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
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
