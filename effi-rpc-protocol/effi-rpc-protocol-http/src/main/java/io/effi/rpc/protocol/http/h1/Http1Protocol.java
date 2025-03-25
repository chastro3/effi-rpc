package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.support.handler.ClientMessageAggregator;
import io.effi.rpc.support.handler.ServerMessageAggregator;
import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NamedChannelHandler;
import io.effi.rpc.transport.NettySupport;
import io.effi.rpc.transport.SslContextFactory;
import io.effi.rpc.transport.client.Client;
import io.effi.rpc.transport.server.Server;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

import static io.effi.rpc.common.constant.Component.Protocol.HTTP;
import static io.effi.rpc.common.constant.Component.Protocol.HTTPS;

/**
 * Http protocol.
 */
@Extension({HTTP, HTTPS})
public class Http1Protocol extends HttpProtocol {

    public Http1Protocol() {
        super(HttpVersion.HTTP_1_1);
    }

    public Http1Client acquireClient(ClientConfig config) {
        String key = config.config().get(KeyConstant.NAME, config.protocol());
        return (Http1Client) clients.get(key);
    }

    @Override
    protected Client connect(InitializedConfig config) {
        return new Http1Client(config);
    }

    @Override
    protected Server bind(InitializedConfig config) {
        return new Http1Server(config);
    }

    @Override
    protected InitializedConfig initClientConfig(URL url, EffiRpcModule module) {
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
    protected InitializedConfig initServerConfig(URL url, EffiRpcModule module) {
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

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, Config config) {
        return new Http1CalleeBuilder<T>(methodMapper, config).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, EffiRpcModule module, Locator locator, Config config) {
        return new Http1CallerBuilder<>(returnType, config)
                .module(module)
                .locator(locator)
                .build();
    }
}
