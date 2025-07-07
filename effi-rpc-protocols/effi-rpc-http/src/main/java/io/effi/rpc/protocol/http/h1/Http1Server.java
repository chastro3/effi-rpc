package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.transport.netty.NettyServer;
import io.effi.rpc.transport.netty.SslContextFactory;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Server} using http1.1.
 */
public class Http1Server extends NettyServer {

    private Http1ServerHandler serverHandler;

    public Http1Server(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        initialBootStrap();
        channelConfigurer(new Http1ServerChannelConfigurer(this));
        sslContext(SslContextFactory.getOrCreateForServer(H1Support.SUPPORTED_PROTOCOL, config));
        this.serverHandler = new Http1ServerHandler();
    }

    protected void http1serverHandler(Http1ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    public Http1ServerHandler http1serverHandler() {
        return serverHandler;
    }

    public HttpServerCodec newHttp1Codec() {
        // todo config?
        return new HttpServerCodec();
    }

    public HttpObjectAggregator newMessageAggregator() {
        int maxMessageSize = url.getIntParam(DefaultConfigNames.MAX_MESSAGE_SIZE);
        return new HttpObjectAggregator(maxMessageSize);
    }

}
