package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NamedChannelHandler;
import io.effi.rpc.transport.NettySupport;
import io.effi.rpc.transport.SslContextFactory;
import io.effi.rpc.transport.client.Client;
import io.effi.rpc.transport.server.Server;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

import static io.effi.rpc.common.constant.Component.Protocol.H2;
import static io.effi.rpc.common.constant.Component.Protocol.H2C;

/**
 * Http2 Protocol.
 */
@Extension({H2, H2C})
public class Http2Protocol extends HttpProtocol {

    public Http2Protocol() {
        super(HttpVersion.HTTP_2_0);
    }

    @Override
    protected Client connect(InitializedConfig config) {
        return NettySupport.isPooledClient(config.url())
                ? new Http2PoolClient(config)
                : new Http2Client(config);
    }

    @Override
    protected Server bind(InitializedConfig config) {
        return new Http2Server(config);
    }

    @Override
    protected InitializedConfig initClientConfig(URL url, EffiRpcModule module) {
        SslContext sslContext = NettySupport
                .acquireSslContext(url, () -> SslContextFactory.acquireForClient(ApplicationProtocolNames.HTTP_2));
        return new InitializedConfig(url, module,
                () -> List.of(
                        new NamedChannelHandler("http2ClientFrameCodec", Http2FrameCodecBuilder.forClient()
                                .initialSettings(H2Support.buildHttp2Settings(url)).build()),
                        // this parameter ChannelInboundHandlerAdapter is Invalid for client
                        new NamedChannelHandler("http2MultiplexHandler", new Http2MultiplexHandler(new ChannelInboundHandlerAdapter()))
                ), sslContext);
    }

    @Override
    protected InitializedConfig initServerConfig(URL url, EffiRpcModule module) {
        SslContext sslContext = NettySupport
                .acquireSslContext(url, () -> SslContextFactory.acquireForServer(ApplicationProtocolNames.HTTP_2));
        return new InitializedConfig(url, module,
                () -> List.of(
                        new NamedChannelHandler("http2FrameServerCodec", Http2FrameCodecBuilder.forServer()
                                .initialSettings(H2Support.buildHttp2Settings(url)).build()),
                        new NamedChannelHandler("http2serverHandler", new Http2MultiplexHandler(new Http2ServerHandler(url, module)))
                ), sslContext);
    }

    @Override
    public <T> Callee<T> createCallee(MethodMapper<T> methodMapper, Config config) {
        return new Http2CalleeBuilder<T>(methodMapper, config).build();
    }

    @Override
    public <T> Caller<T> createCaller(TypeToken<T> returnType, EffiRpcModule module, Locator locator, Config config) {
        return new Http2CallerBuilder<>(returnType, config)
                .module(module)
                .locator(locator)
                .build();
    }
}
