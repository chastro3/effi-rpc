package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.stream.StreamHandle;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;

import java.lang.reflect.Type;
import java.util.concurrent.Flow;

/**
 * Handles one client-side gRPC call over its message stream.
 * <p>
 * Owns the stream handle and message codec so callers only adapt observers.
 *
 * @param <R> reply message type
 */
final class GrpcClientCall<R> {

    private final StreamHandle<GrpcMessage, GrpcMessage> handle;

    private final Serializer serializer;

    private final Type replyType;

    GrpcClientCall(StreamHandle<GrpcMessage, GrpcMessage> handle, Serializer serializer, Type replyType) {
        this.handle = handle;
        this.serializer = serializer;
        this.replyType = replyType;
    }

    void receive(StreamObserver<R> observer, long demand) {
        handle.inbound().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(demand);
            }

            @Override
            public void onNext(GrpcMessage message) {
                observer.onNext(reply(message));
            }

            @Override
            public void onError(Throwable cause) {
                observer.onError(cause);
            }

            @Override
            public void onComplete() {
                observer.onCompleted();
            }
        });
    }

    void send(Object[] values) {
        if (values.length > 0) {
            handle.outbound().onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, values[0])));
        }
        handle.outbound().onComplete();
    }

    void cancel(EffiRpcException cause) {
        handle.outbound().cancel(cause);
    }

    <T> StreamObserver<T> sender() {
        return new StreamObserver<>() {
            @Override
            public void onNext(T value) {
                handle.outbound().onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, value)));
            }

            @Override
            public void onError(Throwable cause) {
                handle.outbound().onError(cause);
            }

            @Override
            public void onCompleted() {
                handle.outbound().onComplete();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private R reply(GrpcMessage message) {
        return (R) GrpcSupport.deserialize(serializer, message.payload(), replyType);
    }
}
