package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.netty.ChannelManageHandler;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.util.AssertUtil;
import io.netty.channel.Channel;

/**
 * Configures server channel for HTTP/1.1 or HTTP/2.
 */
public class HttpServerConfigurer {

    private final NettyEndpointConfig h2config;

    private final NettyEndpointConfig h1config;

    private final ChannelManageHandler channelManager;

    public HttpServerConfigurer(ChannelManageHandler channelManager, NettyEndpointConfig h2config) {
        this.channelManager = AssertUtil.notNull(channelManager, "channelManager");
        this.h2config = AssertUtil.notNull(h2config, "h2config");
        this.h1config = H2Support.getH1config(h2config);
    }

    public void configure(Channel channel, boolean isH2) {
        NettySupport.initServerChannel(channel, isH2 ? h2config : h1config, channelManager);
    }
}
