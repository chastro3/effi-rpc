package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.transport.netty.*;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

public class Http2Transporter implements NettyTransporter {
    @Override
    public Client connect(NettyEndpointConfig config) {
        return NettySupport.isPooledClient(config.url())
                ? new Http2PoolClient(config)
                : new Http2Client(config);
    }

    @Override
    public Server bind(NettyEndpointConfig config) {
        return new Http2Server(config);
    }

    @Override
    public NettyEndpointConfig initClientConfig(URL url, EffiRpcModule module) {
        SslContext sslContext = NettySupport.acquireSslContext(url, () ->
                SslContextFactory.acquireForClient(ApplicationProtocolNames.HTTP_2));
        return new NettyEndpointConfig(url, module, sslContext, () -> List.of(
                new NamedChannelHandler("http2ClientFrameCodec", Http2FrameCodecBuilder.forClient()
                        .initialSettings(H2Support.buildHttp2Settings(url)).build()),
                new NamedChannelHandler("http2MultiplexHandler", new Http2MultiplexHandler(new Http2ClientHandler()))
        ));
    }

    @Override
    public NettyEndpointConfig initServerConfig(URL url, EffiRpcModule module) {
        SslContext sslContext = NettySupport.acquireSslContext(url, () ->
                SslContextFactory.acquireForServer(ApplicationProtocolNames.HTTP_2));
        return new NettyEndpointConfig(url, module, sslContext, () -> List.of(
                new NamedChannelHandler("http2FrameServerCodec", Http2FrameCodecBuilder.forServer()
                        .initialSettings(H2Support.buildHttp2Settings(url)).build()),
                new NamedChannelHandler("http2serverHandler", new Http2MultiplexHandler(new Http2ServerHandler(url, module)))
        ));
    }
}
