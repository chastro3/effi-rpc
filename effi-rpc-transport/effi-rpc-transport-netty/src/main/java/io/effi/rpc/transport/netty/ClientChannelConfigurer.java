package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

/**
 * Provides the base configuration for a Netty client channel.
 */
public abstract class ClientChannelConfigurer<E extends NettyClient> extends ChannelConfigurer<E> {

    protected ClientChannelConfigurer(E client) {
        super(client);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        ChannelPipeline pipeline = channel.pipeline();
        SslContext sslContext = endpoint.sslContext();
        if (sslContext != null && pipeline.get(HandlerNames.SSL) == null)
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        pipeline.addLast(HandlerNames.HEARTBEAT, endpoint.heartBeatHandler());
    }
}
