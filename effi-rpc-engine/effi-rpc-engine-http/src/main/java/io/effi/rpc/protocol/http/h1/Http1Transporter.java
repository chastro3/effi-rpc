package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Constant;
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
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.CLIENT_MAX_RECEIVE_SIZE.key(), Constant.DEFAULT_MAX_MESSAGE_SIZE);
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForClient(ApplicationProtocolNames.HTTP_1_1));
        return new NettyEndpointConfig(url, module, sslContext, () -> List.of(
                new NamedChannelHandler("httpClientCodec", new HttpClientCodec()),
                new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                new NamedChannelHandler("httpClientHandler", new Http1ClientHandler(url)),
                new NamedChannelHandler(ClientMessageAggregator.NAME, new ClientMessageAggregator()))
        );
    }

    @Override
    public NettyEndpointConfig initServerConfig(URL url, EffiRpcModule module) {
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.SERVER_MAX_RECEIVE_SIZE.key(), Constant.DEFAULT_MAX_MESSAGE_SIZE);
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForServer(ApplicationProtocolNames.HTTP_1_1));
        return new NettyEndpointConfig(url, module, sslContext, () -> List.of(
                new NamedChannelHandler("httpServerCodec", new HttpServerCodec()),
                new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                new NamedChannelHandler("httpServerHandler", new Http1ServerHandler()),
                new NamedChannelHandler(ServerMessageAggregator.NAME, new ServerMessageAggregator())
        ));
    }
}
