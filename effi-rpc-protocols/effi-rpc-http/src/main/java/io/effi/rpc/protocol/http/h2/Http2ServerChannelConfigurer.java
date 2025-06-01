package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.transport.netty.ServerChannelConfigurer;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

/**
 * Configures the channel for {@link Http2Server}.
 */
public class Http2ServerChannelConfigurer extends ServerChannelConfigurer<Http2Server> {

    public Http2ServerChannelConfigurer(Http2Server server) {
        super(server);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        super.doConfigure(channel, url);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("http2FrameServerCodec", endpoint.newHttp2Codec());
        pipeline.addLast("http2MultiplexHandler", endpoint.newMultiplexHandler(
                NettySupport.newChannelInitializer(h2ch -> {
                    initChannel(h2ch, url);
                    ChannelPipeline h2pipeline = h2ch.pipeline();
                    NamedChannelHandler messageAggregator = ServerMessageAggregator.getInstance();
                    h2pipeline.addLast("http2ServerHandler", endpoint.http2ServerHandler());
                    h2pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
                })
        ));
    }
}
