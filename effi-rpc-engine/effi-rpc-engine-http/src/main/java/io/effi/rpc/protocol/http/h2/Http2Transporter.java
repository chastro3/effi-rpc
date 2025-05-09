package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.ServerConfig;
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

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Http2 implementation of {@link NettyTransporter}.
 */
public class Http2Transporter implements NettyTransporter {

    private static final String[] SUPPORTED_PROTOCOL = new String[]{ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1};

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
    public NettyEndpointConfig initClientConfig(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcModule module) {
        SslContext sslContext = NettySupport.getOrCreateSslContext(config,
                () -> SslContextFactory.getForClient(SUPPORTED_PROTOCOL, config.certificate()));
        return new NettyEndpointConfig(config, config.newUrl(remoteAddress), module)
                .sslContext(sslContext)
                .codecInitializer(this::initClientCodec)
                .handlersInitializer(cfg -> List.of(
                        // this parameter ChannelInboundHandlerAdapter is Invalid for client
                        new NamedChannelHandler("http2MultiplexHandler", new Http2MultiplexHandler(INBOUND_HANDLER))
        ));
    }

    @Override
    public NettyEndpointConfig initServerConfig(ServerConfig config, InetSocketAddress address, EffiRpcModule module) {
        SslContext sslContext = NettySupport.getOrCreateSslContext(config, () -> SslContextFactory.getForServer(SUPPORTED_PROTOCOL, config.certificate()));
        URL url = config.newUrl(address);
        Http2ServerHandler serverHandler = new Http2ServerHandler(url, module);
        return new NettyEndpointConfig(config, url, module)
                .sslContext(sslContext)
                .codecInitializer(this::initServerCodec)
                .handlersInitializer(cfg -> List.of(
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
