package io.effi.rpc.transport.netty;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.io.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility class for netty operations.
 */
public class NettySupport {

    public static final AttributeKey<URL> REQUEST_URL = AttributeKey.valueOf("requestUrl");

    /**
     * Checks if the given URL config is pooled client.
     */
    public static boolean isPooledClient(URL url) {
        int maxConnections = url.getIntParam(DefaultConfigKeys.MAX_CONNECTIONS);
        return maxConnections > 1;

    }

    /**
     * Converts ByteBuf to byte array.
     */
    public static byte[] getBytes(ByteBuf buf) {
        try {
            return io.netty.buffer.ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false);
        } finally {
            ReferenceCountUtil.release(buf);
        }
    }

    /**
     * Gets ssl bytes.
     */
    public static byte[] getSslBytes(String systemDir, String defaultPath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String caPath = System.getProperty(systemDir);
        InputStream caStream = null;
        try {
            if (StringUtil.isBlank(caPath)) {
                caStream = classLoader.getResourceAsStream(defaultPath);
            } else {
                caStream = new FileInputStream(caPath);
            }
            if (caStream != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = caStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return outputStream.toByteArray();
            }
            return null;
        } finally {
            if (caStream != null) {
                caStream.close();
            }
        }
    }

    /**
     * Converts byte array to InputStream.
     */
    public static InputStream readBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can't null");
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Gets or creates ssl context.
     */
    public static SslContext getOrCreateSslContext(URL url, Supplier<SslContext> creator) {
        boolean sslEnabled = url.getBooleanParam(DefaultConfigKeys.SSL.key(), false);
        return sslEnabled ? creator.get() : null;
    }

    /**
     * Builds server channel initializer.
     */
    public static ChannelInitializer<SocketChannel> buildServerChannelInitializer(NettyEndpointConfig config, ChannelManageHandler channelManager) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                initServerChannel(ch, config, channelManager);
            }
        };
    }

    /**
     * Builds client channel initializer.
     */
    public static ChannelInitializer<SocketChannel> buildClientChannelInitializer(NettyEndpointConfig config) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                initClientChannel(ch, config);
            }
        };
    }

    /**
     * Builds channel pool handler.
     */
    public static ChannelPoolHandler buildChannelPoolHandler(NettyEndpointConfig config) {
        return new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                initClientChannel(ch, config);
            }
        };
    }

    /**
     * Binds config to current channel.
     */
    public static void bindURL(URL url, Channel channel) {
        channel.attr(REQUEST_URL).set(url);
    }

    /**
     * Removes config from current channel.
     */
    public static void unbindURL(Channel channel) {
        Attribute<URL> attr = channel.attr(REQUEST_URL);
        attr.set(null);
    }

    /**
     * Gets config from current channel.
     */
    public static URL getBoundChannel(Channel channel) {
        return channel.attr(REQUEST_URL).get();
    }

    /**
     * Initializes client channel.
     */
    public static void initClientChannel(Channel channel, NettyEndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        NettyChannel.getOrCreate(channel, config.url(), config.module());
        SslContext sslContext = config.sslContext();
        if (sslContext != null)
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        addCommonHandlers(pipeline, config);
    }

    /**
     * Initializes server channel.
     */
    public static void initServerChannel(Channel channel, NettyEndpointConfig config, ChannelManageHandler channelManager) {
        ChannelPipeline pipeline = channel.pipeline();
        NettyChannel.getOrCreate(channel, config.url(), config.module());
        SslContext sslContext = config.sslContext();
        if (sslContext != null && pipeline.get(HandlerNames.SSL) == null)
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        if (channelManager != null)
            pipeline.addLast(ChannelManageHandler.NAME, channelManager);
        addCommonHandlers(pipeline, config);
    }

    /**
     * Removes all handlers from pipeline.
     */
    public static void removeAllHandlers(ChannelPipeline pipeline) {
        List<String> names = pipeline.names();
        for (String name : names) {
            pipeline.remove(name);
        }
    }

    /**
     * Builds request config.
     */
    public static URL buildRequestUrl(URL serverUrl, String path) {
        URL requestUrl = URL.builder()
                .type(URLType.REQUEST)
                .protocol(serverUrl.protocol())
                .address(serverUrl.address())
                .path(path)
                .build();
        requestUrl.addParam(KeyConstant.ONEWAY, Boolean.FALSE.toString());
        return requestUrl;
    }

    private static void addCommonHandlers(ChannelPipeline pipeline, NettyEndpointConfig config) {
        NettyIdleStateHandler idleStateHandler = new NettyIdleStateHandler(config.url(), config.module());
        pipeline.addLast(HandlerNames.IDLE_STATE, idleStateHandler);
        pipeline.addLast(HandlerNames.HEARTBEAT, idleStateHandler.heartBeatHandler());
        NamedChannelHandler codec = config.codec();
        pipeline.addLast(codec.name(), codec.handler());
        List<NamedChannelHandler> handlers = config.handlers();
        handlers.forEach(handler -> pipeline.addLast(handler.name(), handler.handler()));
    }
}
