package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.protocol.http.h1.Http1Server;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Server} using http2.
 */
public class Http2Server extends Http1Server {

    public Http2Server(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        channelConfigurer(new HttpCombineChannelConfigurer(this));
    }

}
