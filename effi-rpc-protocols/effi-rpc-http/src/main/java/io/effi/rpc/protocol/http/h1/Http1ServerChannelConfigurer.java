package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.ServerMessageAggregator;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Configures the channel for {@link Http1Server}.
 */
public class Http1ServerChannelConfigurer extends EndpointChannelConfigurer<Http1Server> {

    private final Http1ServerHandler serverHandler;

    public Http1ServerChannelConfigurer(Http1Server server) {
        super(server, SslContextManager.fetch(H1Support.SUPPORTED_PROTOCOL, server.config()));
        this.serverHandler = new Http1ServerHandler();
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("httpServerCodec", newCodec());
        NamedChannelHandler messageAggregator = ServerMessageAggregator.getInstance();
        pipeline.addLast("httpServerAggregator", newMessageAggregator());
        pipeline.addLast("httpServerHandler", serverHandler);
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
    }

    private HttpServerCodec newCodec() {
        // todo config?
        return new HttpServerCodec();
    }

    private HttpObjectAggregator newMessageAggregator() {
        int maxMessageSize = endpoint.config().getConfig(ConfigNames.MAX_MESSAGE_SIZE);
        return new HttpObjectAggregator(maxMessageSize);
    }
}
