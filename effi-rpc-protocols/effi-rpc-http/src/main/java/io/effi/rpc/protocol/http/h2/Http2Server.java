package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.protocol.http.h1.Http1Server;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.protocol.http.h1.Http1ServerHandler;
import io.effi.rpc.transport.netty.SslContextFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannel;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Server} using http2.
 */
public class Http2Server extends Http1Server {

    private Http2ServerHandler serverHandler;

    private volatile URL http1ServerURL;

    public Http2Server(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        initialBootStrap();
        channelConfigurer(new HttpCombineChannelConfigurer(this));
        sslContext(SslContextFactory.getOrCreateForServer(H2Support.SUPPORTED_PROTOCOL, config));
        this.serverHandler = new Http2ServerHandler();
        http1serverHandler(new Http1ServerHandler());
    }

    @Override
    public Http2ServerConfig config() {
        return (Http2ServerConfig) config;
    }

    public Http2ServerHandler http2ServerHandler() {
        return serverHandler;
    }

    public Http2FrameCodec newHttp2Codec() {
        return Http2FrameCodecBuilder.forServer()
                .initialSettings(H2Support.createHttp2Settings(url))
                .build();
    }

    public Http2MultiplexHandler newMultiplexHandler(ChannelInitializer<Http2StreamChannel> initializer) {
        return new Http2MultiplexHandler(initializer);
    }

    public URL http1ServerURL() {
        if (http1ServerURL == null) {
            synchronized (this) {
                if (http1ServerURL == null) {
                    Http1ServerConfig http1ServerConfig = config().http1ServerConfig();
                    if (http1ServerConfig != null) {
                        http1ServerURL = http1ServerConfig.newUrl(socketAddress());
                    } else {
                        http1ServerURL = Http1ServerConfig.defaultConfig().newUrl(socketAddress());
                    }
                }
            }
        }
        return http1ServerURL;
    }

}
