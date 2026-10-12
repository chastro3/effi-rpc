package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;

/**
 * Provides the channel configuration for {@link Http2Server}.
 */
public class Http2ServerChannelConfigurer extends EndpointChannelConfigurer<Http2Server> {

    public Http2ServerChannelConfigurer(Http2Server server) {
        super(server, SslContextManager.contextOf(H2Support.SUPPORTED_PROTOCOL, server.config()));
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", Http2FrameCodecBuilder.forServer()
                .initialSettings(H2Support.createHttp2Settings(endpoint.config()))
                .build());
        pipeline.addLast("http2MultiplexHandler", new Http2MultiplexHandler(
                NettySupport.newChannelInitializer(h2ch -> configureStreamPipeline(h2ch, config))
        ));
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
        NamedChannelHandler messageAggregator = ServerMessageAggregator.getInstance();
        pipeline.addLast("http2ServerHandler", new Http2ServerHandler());
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
    }
}
