package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.transport.netty.NettyPoolClient;
import io.effi.rpc.transport.netty.SslContextFactory;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Client} using http1.1.
 */
public class Http1Client extends NettyPoolClient {

    private Http1ClientHandler clientHandler;

    public Http1Client(ClientConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        initialBootStrap();
        channelConfigurer(new Http1ClientChannelConfigurer(this));
        sslContext(SslContextFactory.getOrCreateForClient(H1Support.SUPPORTED_PROTOCOL, config));
        this.clientHandler = new Http1ClientHandler(this);
    }

    public Http1ClientHandler clientHandler() {
        return clientHandler;
    }

    public HttpClientCodec newHttp1Codec() {
        // todo config?
        return new HttpClientCodec();
    }

    public HttpObjectAggregator newMessageAggregator() {
        int maxMessageSize = url.getIntParam(DefaultConfigNames.MAX_MESSAGE_SIZE);
        return new HttpObjectAggregator(maxMessageSize);
    }

}
