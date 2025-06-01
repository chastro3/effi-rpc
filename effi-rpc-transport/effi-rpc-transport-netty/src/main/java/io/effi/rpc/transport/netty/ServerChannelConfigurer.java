package io.effi.rpc.transport.netty;

import io.effi.rpc.config.URL;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.ssl.SslContext;

/**
 * Provides the base configuration for a Netty server channel.
 */
public class ServerChannelConfigurer<E extends NettyServer> extends ChannelConfigurer<E> {

    protected ServerChannelConfigurer(E server) {
        super(server);
    }

    @Override
    protected void doConfigure(Channel channel, URL url) {
        SslContext sslContext = endpoint.sslContext();
        ChannelPipeline pipeline = channel.pipeline();
        // Need to check if SSLHandler is already in the pipeline,
        // as HTTP/2 TLS upgrade requires SSL to be configured first.
        if (sslContext != null && pipeline.get(HandlerNames.SSL) == null)
            pipeline.addLast(HandlerNames.SSL, sslContext.newHandler(channel.alloc()));
        pipeline.addLast(ChannelManageHandler.NAME, endpoint.channelManager());
        pipeline.addLast(HandlerNames.HEARTBEAT, endpoint.heartBeatHandler());
    }

}
