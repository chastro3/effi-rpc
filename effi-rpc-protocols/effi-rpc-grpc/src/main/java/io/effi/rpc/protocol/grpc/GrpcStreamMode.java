package io.effi.rpc.protocol.grpc;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.protocol.grpc.client.GrpcClient;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.TransportSupport;

import java.net.InetSocketAddress;

/**
 * Provides the gRPC interaction strategy backed by one message stream.
 */
public final class GrpcStreamMode implements Interaction.Mode {

    public static final GrpcStreamMode INSTANCE = new GrpcStreamMode();

    private GrpcStreamMode() {
    }

    @Override
    public Interaction.Result dispatch(CallContext<Request, Caller<?>> context) {
        Caller<?> caller = context.peer();
        SmartURL requestUrl = context.message().url();
        InetSocketAddress remoteAddress = InetSocketAddress.createUnresolved(requestUrl.host(), requestUrl.port());
        TransportProtocol protocol = TransportSupport.findProtocol(caller);
        GrpcClient client = (GrpcClient) protocol.supplyClient(caller.clientConfig(), remoteAddress, context.platform());
        return Interaction.Result.success(requestUrl, client.openStream(context));
    }
}
