package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.transport.netty.NettyServer;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Server} using http1.1.
 */
public class Http1Server extends NettyServer {

    public Http1Server(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        withChannelConfigurer(new Http1ServerChannelConfigurer(this));
    }

}
