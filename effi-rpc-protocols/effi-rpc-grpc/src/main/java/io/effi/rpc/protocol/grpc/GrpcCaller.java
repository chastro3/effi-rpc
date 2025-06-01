package io.effi.rpc.protocol.grpc;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.protocol.http.h2.Http2Caller;

import java.util.concurrent.CompletableFuture;

public class GrpcCaller<R> extends Http2Caller<R> {

    GrpcCaller(NodeConfig config, Builder<R> builder) {
        super(config, builder);
    }

    public CompletableFuture<R> unaryCall(Object... args) {
        return call(args);
    }

    public void serverStreamCall(StreamObserver<R> responseObserver, Object... args) {

    }

    public <T> StreamObserver<T> clientStreamCall(StreamObserver<R> responseObserver, Object... args) {
        return null;
    }

    public <T> StreamObserver<T> bidirectionalStreamCall(StreamObserver<R> responseObserver, Object... args) {
        return null;
    }

}
