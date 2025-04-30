package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.transport.netty.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;

import java.util.List;

/**
 * Http2 implementation of {@link NettyTransporter}.
 */
public class Http2Transporter implements NettyTransporter {

    private static final ChannelHandler INBOUND_HANDLER = new ChannelInboundHandlerAdapter();
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
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForClient(ApplicationProtocolNames.HTTP_2));
        return new NettyEndpointConfig(url, module)
                .sslContext(sslContext)
                .codecInitializer(this::initClientCodec)
                .handlersInitializer(config -> List.of(
                        // this parameter ChannelInboundHandlerAdapter is Invalid for client
                        new NamedChannelHandler("http2MultiplexHandler", new Http2MultiplexHandler(INBOUND_HANDLER))
        ));
    }

    @Override
    public NettyEndpointConfig initServerConfig(URL url, EffiRpcModule module) {
        SslContext sslContext = NettySupport.getOrCreateSslContext(url, () ->
                SslContextFactory.getForServer(ApplicationProtocolNames.HTTP_2));
        Http2ServerHandler serverHandler = new Http2ServerHandler(url, module);
        return new NettyEndpointConfig(url, module)
                .sslContext(sslContext)
                .codecInitializer(this::initServerCodec)
                .handlersInitializer(config -> List.of(
                        new NamedChannelHandler("http2serverHandler", new Http2MultiplexHandler(serverHandler))
        ));
    }

    private NamedChannelHandler initClientCodec(NettyEndpointConfig config) {
        return new NamedChannelHandler(
                "http2ClientFrameCodec",
                Http2FrameCodecBuilder.forClient()
                        .initialSettings(H2Support.createHttp2Settings(config.url()))
                        .build()
        );
    }

    private NamedChannelHandler initServerCodec(NettyEndpointConfig config) {
        return new NamedChannelHandler(
                "http2FrameServerCodec",
                Http2FrameCodecBuilder.forServer()
                        .initialSettings(H2Support.createHttp2Settings(config.url()))
                        .build());
    }
}
