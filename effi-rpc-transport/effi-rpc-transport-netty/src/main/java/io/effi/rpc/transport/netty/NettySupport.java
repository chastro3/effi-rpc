package io.effi.rpc.transport.netty;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.constant.HandlerNames;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLType;
import io.effi.rpc.common.util.StringUtil;
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
     *
     * @param url
     * @return
     */
    public static boolean isPooledClient(URL url) {
        int maxConnections = url.getIntParam(DefaultConfigKeys.MAX_CONNECTIONS.key(), 1);
        return maxConnections > 1;

    }

    /**
     * Converts ByteBuf to byte array.
     *
     * @param buf
     * @return
     */
    public static byte[] getBytes(ByteBuf buf) {
        return io.netty.buffer.ByteBufUtil.getBytes(buf, buf.readerIndex(), buf.readableBytes(), false);
    }

    /**
     * Gets ssl bytes.
     *
     * @param systemDir
     * @param defaultPath
     * @return
     * @throws IOException
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
     *
     * @param bytes
     * @return
     */
    public static InputStream readBytes(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes can't null");
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Acquires ssl context.
     *
     * @param url
     * @param creator
     * @return
     */
    public static SslContext acquireSslContext(URL url, Supplier<SslContext> creator) {
        boolean sslEnabled = url.getBooleanParam(DefaultConfigKeys.SSL.key(), false);
        return sslEnabled ? creator.get() : null;
    }

    /**
     * Builds client channel initializer.
     *
     * @param config
     * @return
     */
    public static ChannelInitializer<SocketChannel> buildClientChannelInitializer(InitializedConfig config) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                initClientChannel(ch, config);
            }
        };
    }

    /**
     * Builds server channel initializer.
     *
     * @param config
     * @param channelManager
     * @return
     */
    public static ChannelInitializer<SocketChannel> buildServerChannelInitializer(InitializedConfig config, ChannelManageHandler channelManager) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                initServerChannel(ch, config, channelManager);
            }
        };
    }

    /**
     * Builds channel pool handler.
     *
     * @param config
     * @return
     */
    public static ChannelPoolHandler buildChannelPoolHandler(InitializedConfig config) {
        return new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                initClientChannel(ch, config);
            }
        };
    }

    /**
     * Binds url to current channel.
     *
     * @param url
     * @param channel
     */
    public static void bindURL(URL url, Channel channel) {
        channel.attr(REQUEST_URL).set(url);
    }

    /**
     * Removes url from current channel.
     *
     * @param channel
     */
    public static void unbindURL(Channel channel) {
        Attribute<URL> attr = channel.attr(REQUEST_URL);
        attr.set(null);
    }

    /**
     * Acquires url from current channel.
     *
     * @param channel
     * @return
     */
    public static URL acquireBoundChannel(Channel channel) {
        return channel.attr(REQUEST_URL).get();
    }

    /**
     * Initializes client channel.
     *
     * @param channel
     * @param config
     */
    public static void initClientChannel(Channel channel, InitializedConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        SslContext sslContext = config.sslContext();
        if (sslContext != null) {
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        }
        NettyIdleStateHandler idleStateHandler = new NettyIdleStateHandler(config.url(), config.module());
        pipeline.addLast(HandlerNames.IDLE_STATE, idleStateHandler);
        pipeline.addLast(HandlerNames.HEARTBEAT, idleStateHandler.heartBeatHandler());
        List<NamedChannelHandler> handlers = config.initializedHandlers().get();
        handlers.forEach(handler -> pipeline.addLast(handler.name(), handler.handler()));
    }

    /**
     * Initializes server channel.
     *
     * @param channel
     * @param config
     * @param channelManager
     */
    public static void initServerChannel(Channel channel, InitializedConfig config, ChannelManageHandler channelManager) {
        ChannelPipeline pipeline = channel.pipeline();
        SslContext sslContext = config.sslContext();
        if (sslContext != null) {
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        }
        pipeline.addLast(ChannelManageHandler.NAME, channelManager);
        NettyIdleStateHandler idleStateHandler = new NettyIdleStateHandler(config.url(), config.module());
        pipeline.addLast(HandlerNames.IDLE_STATE, idleStateHandler);
        pipeline.addLast(HandlerNames.HEARTBEAT, idleStateHandler.heartBeatHandler());
        List<NamedChannelHandler> handlers = config.initializedHandlers().get();
        handlers.forEach(handler -> pipeline.addLast(handler.name(), handler.handler()));
    }

    /**
     * Builds request url.
     *
     * @param serverUrl
     * @param path
     * @return
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
}
