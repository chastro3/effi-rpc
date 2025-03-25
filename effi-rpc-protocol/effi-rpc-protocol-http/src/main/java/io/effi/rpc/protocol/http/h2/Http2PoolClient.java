package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.client.NettyPoolClient;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

/**
 * Http2 Pool Client.
 */
public class Http2PoolClient extends NettyPoolClient {

    public Http2PoolClient(InitializedConfig config) {
        super(config);
    }

    @Override
    protected ChannelPoolHandler buildChannelPoolHandler() {
        return H2Support.buildChannelPoolHandler(config, new Http2ClientHandler());
    }

    @Override
    public NettyChannel acquireChannel() {
        NettyChannel parentNettyChannel = super.acquireChannel();
        Channel parentChannel = parentNettyChannel.channel();
        Http2StreamChannelBootstrap streamChannelBootstrap = parentChannel.attr(H2Support.H2_STREAM_BOOTSTRAP_KEY).get();
        NettyChannel nettyChannel = H2Support.acquireStreamChannel(streamChannelBootstrap, url(), module(), connectTimeout);
        release(parentChannel);
        return nettyChannel;
    }

}

