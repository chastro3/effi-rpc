package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.transport.netty.ClientMessageAggregator;
import io.effi.rpc.transport.netty.EndpointChannelConfigurer;
import io.effi.rpc.transport.netty.NamedChannelHandler;
import io.effi.rpc.transport.netty.SslContextManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

/**
 * Configures the channel for {@link Http1Client}.
 */
public class Http1ClientChannelConfigurer extends EndpointChannelConfigurer<Http1Client> {

    private final Http1ClientHandler clientHandler;

    public Http1ClientChannelConfigurer(Http1Client client) {
        super(client, SslContextManager.fetch(H1Support.SUPPORTED_PROTOCOL, client.config()));
        this.clientHandler = new Http1ClientHandler(client);
    }

    @Override
    protected void doConfigure(Channel channel, EndpointConfig config) {
        super.doConfigure(channel, config);
        ChannelPipeline pipeline = channel.pipeline();
        NamedChannelHandler messageAggregator = ClientMessageAggregator.getInstance();
        pipeline.addLast("httpClientCodec", newCodec());
        pipeline.addLast("httpClientAggregator", newMessageAggregator());
        pipeline.addLast("httpClientHandler", clientHandler);
        pipeline.addLast(messageAggregator.name(), messageAggregator.handler());
    }

    public HttpClientCodec newCodec() {
        // todo config?
        return new HttpClientCodec();
    }

    public HttpObjectAggregator newMessageAggregator() {
        int maxMessageSize = endpoint.config().getConfig(ConfigNames.MAX_MESSAGE_SIZE);
        return new HttpObjectAggregator(maxMessageSize);
    }
}
