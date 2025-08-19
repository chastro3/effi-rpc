package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettyPoolClient;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Client} using http2.
 */
public class Http2Client extends NettyPoolClient {

    public Http2Client(ClientConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        withChannelConfigurer(new Http2ClientChannelConfigurer(this));
    }

    @Override
    public Promise<NettyChannel> fetchChannel() {
        return super.fetchChannel().compose(channel -> {
            Channel physicalChannel = channel.channel();
            Http2StreamChannelBootstrap bootstrap = H2Support.getBoundStreamBootstrap(physicalChannel);
            Promise<NettyChannel> h2ChannelFuture = NettyChannel.wrap(bootstrap.open());
            release(physicalChannel);
            return h2ChannelFuture;
        });
    }

}

