package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.protocol.http.h1.Http1ServerChannelConfigurer;
import io.effi.rpc.protocol.http.h1.Http1ServerConfig;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

/**
 * Configures server channels with HTTP/1 and HTTP/2 support.
 * Combines both strategies and selects proper HTTP version based on context.
 */
public class HttpCombineChannelConfigurer extends EndpointChannelConfigurer<Http2Server> {

    private final Http1ServerChannelConfigurer http1Configurer;

    private final Http2ServerChannelConfigurer http2Configurer;

    public HttpCombineChannelConfigurer(Http2Server server) {
        super(server, SslContextManager.contextOf(H2Support.SUPPORTED_PROTOCOL, server.config()));
        this.http1Configurer = new Http1ServerChannelConfigurer(server);
        this.http2Configurer = new Http2ServerChannelConfigurer(server);
    }

    /**
     * todo 换成http version
     */
    public void configureChannel(Channel channel, boolean isHttp2) {
        if (isHttp2) {
            http2Configurer.configure(channel, endpoint.config());
        } else {
            http1Configurer.configure(channel, findHttp1ServerConfig());
        }
    }

    @Override
    public void configure(Channel channel, EndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        if (sslContext != null) {
            configureSslHandlerIfAbsent(pipeline);
            pipeline.addLast(new HttpNegotiationHandler(this));
        } else {
            pipeline.addLast(new HttpClearTextSniffHandler(this));
        }
    }

    private Http1ServerConfig findHttp1ServerConfig() {
        ServerConfig config = endpoint.config();
        if (config instanceof Http2ServerConfig http2ServerConfig) {
            Http1ServerConfig http1ServerConfig = http2ServerConfig.http1ServerConfig();
            if (http1ServerConfig != null) return http1ServerConfig;
        }
        return Http1ServerConfig.defaultConfig();
    }
}
