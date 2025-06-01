package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettyPoolClient;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.transport.netty.SslContextFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Client} using http2.
 */
public class Http2Client extends NettyPoolClient {

    private static final ChannelHandler INBOUND_HANDLER = new ChannelInboundHandlerAdapter();

    private Http2ClientHandler clientHandler;

    public Http2Client(ClientConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        initialBootStrap();
        channelConfigurer(new Http2ClientChannelConfigurer(this));
        sslContext(SslContextFactory.getOrCreateForClient(H2Support.SUPPORTED_PROTOCOL, config));
        this.clientHandler = new Http2ClientHandler();
    }

    @Override
    public CompletableFuture<io.effi.rpc.transport.endpoint.Channel> getChannel() {
        return super.getChannel().thenCompose(channel -> {
            NettyChannel nettyChannel = (NettyChannel) channel;
            Channel nioChannel = nettyChannel.channel();
            Http2StreamChannelBootstrap bootstrap = H2Support.getBoundStreamBootstrap(nioChannel);
            var result = NettySupport.wrap(bootstrap.open(), this);
            release(nioChannel);
            return result;
        });
    }

    public Http2ClientHandler clientHandler() {
        return clientHandler;
    }

    public Http2FrameCodec newHttp2Codec() {
        return Http2FrameCodecBuilder.forClient()
                .initialSettings(H2Support.createHttp2Settings(url))
                .build();
    }

    public Http2MultiplexHandler newMultiplexHandler() {
        return new Http2MultiplexHandler(INBOUND_HANDLER);
    }
}

