package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.transport.netty.ClientChannelConfigurer;
import io.effi.rpc.transport.netty.ClientMessageAggregator;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

/**
 * Configures the channel for {@link Http2Client}.
 */
public class Http2ClientChannelConfigurer extends ClientChannelConfigurer<Http2Client> {

    public Http2ClientChannelConfigurer(Http2Client client) {
        super(client);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        super.doConfigure(channel, url);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", endpoint.newHttp2Codec());
        pipeline.addLast("http2MultiplexHandler", endpoint.newMultiplexHandler());
        var streamChannelBootstrap = new Http2StreamChannelBootstrap(channel)
                .handler(NettySupport.newChannelInitializer(h2ch -> {
                    initChannel(h2ch, url);
                    ChannelPipeline h2p = h2ch.pipeline();
                    NamedChannelHandler messageAggregator = ClientMessageAggregator.getInstance();
                    h2p.addLast("http2ClientHandler", endpoint.clientHandler());
                    h2p.addLast(messageAggregator.name(), messageAggregator.handler());
                }));
        H2Support.bindStreamBootstrap(channel, streamChannelBootstrap);
    }
}
