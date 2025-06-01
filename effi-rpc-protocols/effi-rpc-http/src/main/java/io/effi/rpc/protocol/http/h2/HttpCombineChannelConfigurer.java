package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.protocol.http.h1.Http1ServerChannelConfigurer;
import io.effi.rpc.transport.netty.HandlerNames;
import io.effi.rpc.transport.netty.ServerChannelConfigurer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

/**
 * Configures server channels with HTTP/1 and HTTP/2 support.
 * Combines both strategies and selects proper HTTP version based on context.
 */
public class HttpCombineChannelConfigurer extends ServerChannelConfigurer<Http2Server> {

    private final Http1ServerChannelConfigurer http1Configurer;

    private final Http2ServerChannelConfigurer http2Configurer;

    public HttpCombineChannelConfigurer(Http2Server server) {
        super(server);
        this.http1Configurer = new Http1ServerChannelConfigurer(server);
        this.http2Configurer = new Http2ServerChannelConfigurer(server);
    }

    public void configureChannel(Channel channel, boolean isHttp2) {
        if (isHttp2) {
            http2Configurer.configure(channel, endpoint.url());
        } else {
            http1Configurer.configure(channel, endpoint.http1ServerURL());
        }
    }

    @Override
    public void configure(Channel channel, URL url) {
        SslContext sslContext = endpoint.sslContext();
        if (sslContext != null) {
            configureSsl(channel, sslContext);
        } else {
            configureClearText(channel);
        }
    }

    private void configureSsl(Channel channel, SslContext sslContext) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        pipeline.addLast(new HttpNegotiationHandler(this));
    }

    private void configureClearText(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpClearTextSniffHandler(this));
    }
}
