package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.client.NettyClient;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

import java.net.ConnectException;

/**
 * Http2Client Base on Netty.
 */
public class Http2Client extends NettyClient {

    private Http2StreamChannelBootstrap streamChannelBootstrap;

    public Http2Client(InitializedConfig config) {
        super(config);

    }

    @Override
    protected void doConnect() throws ConnectException {
        super.doConnect();
        streamChannelBootstrap = channel.attr(H2Support.H2_STREAM_BOOTSTRAP_KEY).get();
    }

    @Override
    protected ChannelInitializer<SocketChannel> buildChannelInitializer() {
        return H2Support.buildClietnChannelInitializer(config, new Http2ClientHandler());
    }

    @Override
    public NettyChannel acquireChannel() {
        return H2Support.acquireStreamChannel(streamChannelBootstrap, url(), module(), connectTimeout);
    }

}
