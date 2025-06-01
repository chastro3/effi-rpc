package io.effi.rpc.transport.netty;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.SystemProperties;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKey;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Implements {@link Client} using Netty.
 */
public class NettyClient extends AbstractNettyEndpoint<NettyClient, Bootstrap> implements Client {

    public static final GenericKey<NioEventLoopGroup> EVENT_LOOP_GROUP_KEY = GenericKey.valueOf("nio-event-loop-group");

    protected static final NioEventLoopGroup NIO_EVENT_LOOP_GROUP = getNioEventLoopGroup();

    private final Object lock = new Object();

    protected volatile CompletableFuture<Channel> connectedFuture;

    public NettyClient(ClientConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform, new Bootstrap());
    }

    @Override
    public CompletableFuture<Channel> getChannel() {
        if (connectedFuture == null) {
            synchronized (lock) {
                if (connectedFuture == null) {
                    connectedFuture = NettySupport.wrap(bootstrap.connect(), this);
                    connectedFuture.thenAccept(channel -> {
                        this.channel = (NettyChannel) channel;
                    });
                }
            }
        }
        return connectedFuture;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    public void close() {
        if (channel != null) channel.close();
    }

    @Override
    public ClientConfig config() {
        return (ClientConfig) config;
    }

    protected void configureOptions(Bootstrap bootstrap) {
        int connectTimeout = url().getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT);
        bootstrap.group(NIO_EVENT_LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .remoteAddress(socketAddress())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
        configureIfValid(DefaultConfigKeys.SEND_BUFFER_SIZE, Integer::parseInt, val -> {
            bootstrap.option(ChannelOption.SO_SNDBUF, val);
        });
        configureIfValid(DefaultConfigKeys.RECEIVE_BUFFER_SIZE, Integer::parseInt, val -> {
            bootstrap.option(ChannelOption.SO_RCVBUF, val);
        });
        configureIfValid(DefaultConfigKeys.NO_DELAY, Boolean::parseBoolean, val -> {
            bootstrap.option(ChannelOption.TCP_NODELAY, val);
        });
        configureIfValid(DefaultConfigKeys.KEEP_ALIVE, Boolean::parseBoolean, val -> {
            bootstrap.option(ChannelOption.SO_KEEPALIVE, val);
        });
    }

    @Override
    protected void configureChannelHandler(Bootstrap bootstrap) {
        bootstrap.handler(NettySupport.newChannelInitializer(this::configureChannel));
    }

    private static NioEventLoopGroup getNioEventLoopGroup() {
        String ioThreadsStr = SystemProperties.getInstance().getParam(SystemKey.CLIENT_IO_THREADS);
        int ioThreads = Constant.DEFAULT_IO_THREADS;
        if (StringUtil.isNotBlank(ioThreadsStr)) {
            try {
                ioThreads = Math.max(Integer.parseInt(ioThreadsStr), ioThreads);
            } catch (NumberFormatException ignore) {
            }
        }
        int finalIoThreads = ioThreads;
        return EffiRpcPlatform.currentPlatform().getOrCreate(
                EVENT_LOOP_GROUP_KEY,
                () -> new NioEventLoopGroup(finalIoThreads, new DefaultThreadFactory("netty-client-worker", false)),
                NioEventLoopGroup::shutdownGracefully
        );
    }
}

