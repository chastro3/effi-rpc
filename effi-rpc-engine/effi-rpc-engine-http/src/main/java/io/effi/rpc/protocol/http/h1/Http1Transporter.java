package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.transport.netty.*;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

/**
 * Http1 implementation of {@link NettyTransporter}.
 */
public class Http1Transporter implements NettyTransporter {

    public static final Http1Transporter INSTANCE = new Http1Transporter();

    @Override
    public Client connect(NettyEndpointConfig config) {
        return new Http1Client(config);
    }

    @Override
    public Server bind(NettyEndpointConfig config) {
        return new Http1Server(config);
    }

    @Override
    public NettyEndpointConfig initClientConfig(URL url, EffiRpcModule module) {
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.CLIENT_MAX_RECEIVE_SIZE);
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForClient(ApplicationProtocolNames.HTTP_1_1));
        return new NettyEndpointConfig(url, module)
                .sslContext(sslContext)
                .codecInitializer(this::initClientCodec)
                .handlersInitializer(config -> List.of(
                        new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                        Http1ClientHandler.getInstance(),
                        ClientMessageAggregator.getInstance())
                );
    }

    @Override
    public NettyEndpointConfig initServerConfig(URL url, EffiRpcModule module) {
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.SERVER_MAX_RECEIVE_SIZE);
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForServer(ApplicationProtocolNames.HTTP_1_1));
        return new NettyEndpointConfig(url, module)
                .sslContext(sslContext)
                .codecInitializer(this::initServerCodec)
                .handlersInitializer(config -> List.of(
                        new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                        Http1ServerHandler.getInstance(),
                        ServerMessageAggregator.getInstance()
                ));
    }

    private NamedChannelHandler initClientCodec(NettyEndpointConfig config) {
        return new NamedChannelHandler("httpClientCodec", new HttpClientCodec());
    }

    private NamedChannelHandler initServerCodec(NettyEndpointConfig config) {
        return new NamedChannelHandler("httpServerCodec", new HttpServerCodec());
    }
}
