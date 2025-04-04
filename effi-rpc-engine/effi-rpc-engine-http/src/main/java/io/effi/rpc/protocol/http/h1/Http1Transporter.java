package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.handler.ClientMessageAggregator;
import io.effi.rpc.protocol.handler.ServerMessageAggregator;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.transport.netty.*;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

public class Http1Transporter implements NettyTransporter {

    @Override
    public Client connect(InitializedConfig config) {
        return new Http1Client(config);
    }

    @Override
    public Server bind(InitializedConfig config) {
        return new Http1Server(config);
    }

    @Override
    public InitializedConfig initClientConfig(URL url, EffiRpcModule module) {
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.CLIENT_MAX_RECEIVE_SIZE.key(), Constant.DEFAULT_MAX_MESSAGE_SIZE);
        SslContext sslContext = NettySupport
                .acquireSslContext(url, () -> SslContextFactory.acquireForClient(ApplicationProtocolNames.HTTP_1_1));
        return new InitializedConfig(url, module,
                () -> List.of(
                        new NamedChannelHandler("httpClientCodec", new HttpClientCodec()),
                        new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                        new NamedChannelHandler("httpClientHandler", new Http1ClientHandler(url)),
                        new NamedChannelHandler(ClientMessageAggregator.NAME, new ClientMessageAggregator())
                ), sslContext);
    }

    @Override
    public InitializedConfig initServerConfig(URL url, EffiRpcModule module) {
        int maxReceiveSize = url.getIntParam(DefaultConfigKeys.SERVER_MAX_RECEIVE_SIZE.key(), Constant.DEFAULT_MAX_MESSAGE_SIZE);
        SslContext sslContext = NettySupport
                .acquireSslContext(url, () -> SslContextFactory.acquireForServer(ApplicationProtocolNames.HTTP_1_1));
        return new InitializedConfig(url, module,
                () -> List.of(
                        new NamedChannelHandler("httpServerCodec", new HttpServerCodec()),
                        new NamedChannelHandler("aggregator", new HttpObjectAggregator(maxReceiveSize)),
                        new NamedChannelHandler("httpServerHandler", new Http1ServerHandler()),
                        new NamedChannelHandler(ServerMessageAggregator.NAME, new ServerMessageAggregator())
                ), sslContext);
    }
}
