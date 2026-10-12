package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.protocol.http.h2.Http2Server;

import java.net.InetSocketAddress;

/**
 * Provides the gRPC server endpoint.
 */
public class GrpcServer extends Http2Server {

    public GrpcServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        channelConfigurer(new GrpcServerChannelConfigurer(this));
    }
}
