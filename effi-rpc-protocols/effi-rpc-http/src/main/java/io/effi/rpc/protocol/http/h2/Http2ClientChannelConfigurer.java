package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.netty.ClientMessageAggregator;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

/**
 * Configures the channel for {@link Http2Client}.
 */
public class Http2ClientChannelConfigurer extends EndpointChannelConfigurer<Http2Client> {

    private static final ChannelHandler INBOUND_HANDLER = new ChannelInboundHandlerAdapter();

    private final Http2ClientHandler clientHandler;

    public Http2ClientChannelConfigurer(Http2Client client) {
        super(client, SslContextManager.contextOf(H2Support.SUPPORTED_PROTOCOL, client.config()));
        this.clientHandler = new Http2ClientHandler();
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        super.doConfigure(channel, config);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", newCodec());
        pipeline.addLast("http2MultiplexHandler", new Http2MultiplexHandler(INBOUND_HANDLER));
        var streamChannelBootstrap = new Http2StreamChannelBootstrap(channel)
                .handler(NettySupport.newChannelInitializer(h2ch -> {
                    initChannel(h2ch, config, true);
                    ChannelPipeline h2p = h2ch.pipeline();
                    NamedChannelHandler messageAggregator = ClientMessageAggregator.getInstance();
                    h2p.addLast("http2ClientHandler", clientHandler);
                    h2p.addLast(messageAggregator.name(), messageAggregator.handler());
                }));
        H2Support.bindStreamBootstrap(channel, streamChannelBootstrap);
    }

    private Http2FrameCodec newCodec() {
        return Http2FrameCodecBuilder.forClient()
                .initialSettings(H2Support.createHttp2Settings(endpoint.config(), true))
                .build();
    }
}
