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
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

/**
 * Provides the channel configuration for {@link Http2Client}.
 */
public class Http2ClientChannelConfigurer extends EndpointChannelConfigurer<Http2Client> {

    private static final ChannelHandler INBOUND_HANDLER = new ChannelInboundHandlerAdapter();

    public Http2ClientChannelConfigurer(Http2Client client) {
        super(client, SslContextManager.contextOf(H2Support.SUPPORTED_PROTOCOL, client.config()));
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", Http2FrameCodecBuilder.forClient()
                .initialSettings(H2Support.createHttp2Settings(endpoint.config()))
                .build());
        pipeline.addLast("http2MultiplexHandler", new Http2MultiplexHandler(INBOUND_HANDLER));
        var streamChannelBootstrap = new Http2StreamChannelBootstrap(channel)
                .handler(NettySupport.newChannelInitializer(h2ch -> configureStreamPipeline(h2ch, config)));
        H2Support.bindStreamBootstrap(channel, streamChannelBootstrap);
    }

    /**
     * Configures the pipeline attached to each HTTP/2 stream channel.
     *
     * @param streamChannel stream channel
     * @param config        endpoint configuration
     */
    protected void configureStreamPipeline(Channel streamChannel, EndpointConfig config) {
        initChannel(streamChannel, config, true);
        ChannelPipeline pipeline = streamChannel.pipeline();
        NamedChannelHandler messageAggregator = ClientMessageAggregator.getInstance();
        pipeline.addLast("http2ClientHandler", new Http2ClientHandler());
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
    }

}
