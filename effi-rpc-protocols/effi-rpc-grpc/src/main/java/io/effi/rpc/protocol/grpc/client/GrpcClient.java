package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.protocol.http.h2.Http2Client;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.stream.GrpcClientStream;
import io.effi.rpc.protocol.grpc.stream.StreamHandle;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.netty.NettyChannel;

import java.net.InetSocketAddress;

/**
 * Provides the gRPC client endpoint.
 */
public class GrpcClient extends Http2Client {

    public GrpcClient(ClientConfig config, InetSocketAddress address, ScopedPlatform platform) {
        super(config, address, platform);
    }

    /**
     * Opens one gRPC stream over a channel of this client.
     *
     * @param context call context
     * @return streaming handle
     */
    public StreamHandle<?, ?> openStream(CallContext<Request, Caller<?>> context) {
        NettyChannel nettyChannel = fetchChannelBlocking();
        GrpcClientStream stream = nettyChannel.channel().attr(GrpcSupport.CLIENT_STREAM).get();
        if (stream == null) {
            throw TransportErrorCodes.FETCH_CHANNEL.fail(nettyChannel.remoteAddress(), GrpcProtocol.NAME);
        }
        stream.start(context.message());
        return new StreamHandle<>(stream, stream);
    }

    @Override
    protected void initialize() {
        configureBootStrap();
        channelConfigurer(new GrpcClientChannelConfigurer(this));
    }

    private NettyChannel fetchChannelBlocking() {
        try {
            Result<NettyChannel> outcome = fetchChannel().await();
            if (outcome.failed()) {
                throw outcome.cause();
            }
            return outcome.value();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw TransportErrorCodes.FETCH_CHANNEL.fail(e, remoteAddress(), GrpcProtocol.NAME);
        }
    }
}
