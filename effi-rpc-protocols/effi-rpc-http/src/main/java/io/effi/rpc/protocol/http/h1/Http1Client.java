package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.transport.netty.NettyPoolClient;

import java.net.InetSocketAddress;

/**
 * Implements {@link io.effi.rpc.transport.endpoint.Client} using http1.1.
 */
public class Http1Client extends NettyPoolClient {

    public Http1Client(ClientConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        channelConfigurer(new Http1ClientChannelConfigurer(this));
    }

}
