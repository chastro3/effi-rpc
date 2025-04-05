package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettyPoolClient;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

/**
 * Http2 Pool Client.
 */
public class Http2PoolClient extends NettyPoolClient {

    public Http2PoolClient(NettyEndpointConfig config) {
        super(config);
    }

    @Override
    protected ChannelPoolHandler buildChannelPoolHandler() {
        return H2Support.buildChannelPoolHandler(config, new Http2ClientHandler());
    }

    @Override
    public io.effi.rpc.transport.endpoint.Channel getChannel() {
        NettyChannel parentNettyChannel = (NettyChannel) super.getChannel();
        Channel parentChannel = parentNettyChannel.channel();
        Http2StreamChannelBootstrap streamChannelBootstrap = parentChannel.attr(H2Support.H2_STREAM_BOOTSTRAP_KEY).get();
        NettyChannel nettyChannel = H2Support.getOrCreateStreamChannel(streamChannelBootstrap, url(), module(), connectTimeout);
        release(parentChannel);
        return nettyChannel;
    }

}

