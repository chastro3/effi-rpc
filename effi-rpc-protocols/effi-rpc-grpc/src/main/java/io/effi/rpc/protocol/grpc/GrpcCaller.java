package io.effi.rpc.protocol.grpc;

import io.effi.rpc.protocol.http.h2.Http2Caller;
import io.effi.rpc.concurrent.Future;

public class GrpcCaller<R> extends Http2Caller<R> {

    GrpcCaller(Builder<R> builder) {
        super(builder);
    }

    public Future<R> unaryCall(Object... args) {
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
