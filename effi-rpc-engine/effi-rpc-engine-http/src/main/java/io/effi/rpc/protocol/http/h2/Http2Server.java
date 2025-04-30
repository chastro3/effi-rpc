package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.netty.ChannelManageHandler;
import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettyServer;

/**
 * Http2 Server.
 */
public class Http2Server extends NettyServer {

    public Http2Server(NettyEndpointConfig config) {
        super(config);
    }

    @Override
    protected void configureChildHandler(ChannelManageHandler channelManageHandler) {
        bootstrap.childHandler(H2Support.buildServerChannelInitializer(config, channelManageHandler));
    }
}
