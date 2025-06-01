package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.URL;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.ServerChannelConfigurer;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

/**
 * Configures the channel for {@link Http1Server}.
 */
public class Http1ServerChannelConfigurer extends ServerChannelConfigurer<Http1Server> {

    public Http1ServerChannelConfigurer(Http1Server server) {
        super(server);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        super.doConfigure(channel, url);
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("httpServerCodec", endpoint.newHttp1Codec());
        NamedChannelHandler messageAggregator = ServerMessageAggregator.getInstance();
        pipeline.addLast("httpServerAggregator", endpoint.newMessageAggregator());
        pipeline.addLast("httpServerHandler", endpoint.http1serverHandler());
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
    }
}
