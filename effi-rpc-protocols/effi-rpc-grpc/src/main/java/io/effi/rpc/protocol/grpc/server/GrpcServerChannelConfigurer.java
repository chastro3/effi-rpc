package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.protocol.http.h2.Http2ServerChannelConfigurer;
import io.netty.channel.Channel;

/**
 * Provides gRPC server stream channel configuration.
 */
public class GrpcServerChannelConfigurer extends Http2ServerChannelConfigurer {

    public GrpcServerChannelConfigurer(GrpcServer server) {
        super(server);
    }

    @Override
    protected void configureStreamPipeline(Channel streamChannel, EndpointConfig config) {
        initChannel(streamChannel, config, true);
        streamChannel.pipeline().addLast("grpcCallResolver", new GrpcCallResolver((GrpcServer) endpoint));
    }
}
