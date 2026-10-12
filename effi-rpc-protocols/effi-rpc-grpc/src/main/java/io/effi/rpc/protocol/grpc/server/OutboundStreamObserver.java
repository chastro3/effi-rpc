package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.stream.GrpcServerStream;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an observer adapter that writes gRPC messages to the server stream.
 *
 * @param <T> outbound message type
 */
public final class OutboundStreamObserver<T> implements StreamObserver<T> {

    private final GrpcServerStream stream;

    private final Serializer serializer;

    private final AtomicBoolean completed = new AtomicBoolean(false);

    OutboundStreamObserver(GrpcServerStream stream, Serializer serializer) {
        this.stream = stream;
        this.serializer = serializer;
    }

    @Override
    public void onNext(T value) {
        stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, value)));
    }

    @Override
    public void onError(Throwable cause) {
        stream.onError(cause);
    }

    @Override
    public void onCompleted() {
        if (completed.compareAndSet(false, true)) {
            stream.onComplete();
        }
    }

    boolean completed() {
        return completed.get();
    }
}
