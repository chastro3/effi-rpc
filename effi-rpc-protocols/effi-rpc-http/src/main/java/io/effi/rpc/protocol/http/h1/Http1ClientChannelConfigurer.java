package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.URL;
import io.effi.rpc.transport.netty.ClientMessageAggregator;
import io.effi.rpc.transport.netty.ClientChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

/**
 * Configures the channel for {@link Http1Client}.
 */
public class Http1ClientChannelConfigurer extends ClientChannelConfigurer<Http1Client> {

    public Http1ClientChannelConfigurer(Http1Client client) {
        super(client);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        super.doConfigure(channel, url);
        ChannelPipeline pipeline = channel.pipeline();
        NamedChannelHandler messageAggregator = ClientMessageAggregator.getInstance();
        pipeline.addLast("httpClientCodec", endpoint.newHttp1Codec());
        pipeline.addLast("httpClientAggregator", endpoint.newMessageAggregator());
        pipeline.addLast("httpClientHandler", endpoint.clientHandler());
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());

    }
}
