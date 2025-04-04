package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.transport.endpoint.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * Netty implementation of {@link Client}.
 */
public class NettyClient extends AbstractClient {

    protected static final NioEventLoopGroup NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup(Constant.DEFAULT_IO_THREADS, new DefaultThreadFactory("netty-client-worker", false));

    protected InitializedConfig config;

    protected Bootstrap bootstrap;

    protected Channel channel;

    public NettyClient(InitializedConfig config) {
        super(config.url(), config.module());
        this.config = config;
        connect();
    }

    /**
     * Closes the NioEventLoopGroup gracefully, releasing all resources.
     */
    public static void closeNioEventLoopGroup() {
        NIO_EVENT_LOOP_GROUP.shutdownGracefully();
    }

    @Override
    protected void doInit() {
        bootstrap = new Bootstrap();
        // Configure the bootstrap options
        bootstrap.group(NIO_EVENT_LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        configHandler();
    }

    protected void configHandler() {
        bootstrap.handler(buildChannelInitializer());
    }

    protected ChannelInitializer<SocketChannel> buildChannelInitializer() {
        return NettySupport.buildClientChannelInitializer(config);
    }

    @Override
    protected void doConnect() throws ConnectException {
        ChannelFuture future = bootstrap.connect();
        // Wait for the connection to complete
        boolean success = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
        // Check the outcome of the connection attempt
        if (success && future.isSuccess()) {
            channel = future.channel();
        } else if (future.cause() != null) {
            ConnectException connectException = new ConnectException();
            connectException.initCause(future.cause());
            throw connectException;
        } else {
            throw new ConnectException("Connect to " + url().address() + " timeout");
        }
    }

    @Override
    public void close() {
        channel.close();
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public io.effi.rpc.transport.endpoint.Channel acquireChannel() {
        return NettyChannel.acquire(channel, url(), module());
    }
}

