package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.protocol.http.HttpOptions;
import io.effi.rpc.protocol.http.h2.Http2Client;
import io.effi.rpc.protocol.http.h2.Http2ClientChannelConfigurer;
import io.netty.channel.Channel;

/**
 * Provides gRPC client stream channel configuration.
 */
public class GrpcClientChannelConfigurer extends Http2ClientChannelConfigurer {

    public GrpcClientChannelConfigurer(Http2Client client) {
        super(client);
    }


    @Override
    protected void configureStreamPipeline(Channel streamChannel, EndpointConfig config) {
        initChannel(streamChannel, config, true);
        streamChannel.pipeline().addLast(
                "grpcClientHandler",
                new GrpcClientHandler(endpoint.config().option(HttpOptions.MAX_MESSAGE_SIZE))
        );
    }
}
