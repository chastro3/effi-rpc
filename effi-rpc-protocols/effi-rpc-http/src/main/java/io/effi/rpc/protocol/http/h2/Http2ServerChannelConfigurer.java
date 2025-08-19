package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;

/**
 * Configures the channel for {@link Http2Server}.
 */
public class Http2ServerChannelConfigurer extends EndpointChannelConfigurer<Http2Server> {

    private final Http2ServerHandler serverHandler;

    public Http2ServerChannelConfigurer(Http2Server server) {
        super(server, SslContextManager.fetch(H2Support.SUPPORTED_PROTOCOL, server.config()));
        this.serverHandler = new Http2ServerHandler();
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", newCodec());
        pipeline.addLast("http2MultiplexHandler", new Http2MultiplexHandler(
                NettySupport.newChannelInitializer(h2ch -> {
                    initChannel(h2ch, config, true);
                    ChannelPipeline h2pipeline = h2ch.pipeline();
                    NamedChannelHandler messageAggregator = ServerMessageAggregator.getInstance();
                    h2pipeline.addLast("http2ServerHandler", serverHandler);
                    h2pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
                })
        ));
    }

    private Http2FrameCodec newCodec() {
        return Http2FrameCodecBuilder.forServer()
                .initialSettings(H2Support.createHttp2Settings(endpoint.config(), false))
                .build();
    }
}
