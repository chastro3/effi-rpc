package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.invocation.MethodInvocation;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.grpc.GrpcMethod;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcStreamMode;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.stream.StreamHandle;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.serialization.protobuf.ProtobufSerializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Type;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;

/**
 * Provides the gRPC caller API for all four call types.
 *
 * @param <R> reply message type
 */
public class GrpcCaller<R> extends HttpCaller<R> {

    protected GrpcCaller(Builder<R> builder) {
        super(builder);
    }

    public static <R> Builder<R> builder(TypeCapture<R> returnType) {
        return new Builder<R>(returnType);
    }

    /**
     * Dispatches one interface-style call to the interaction matching its method signature.
     * <p>
     * A method returning {@link StreamObserver} yields a request observer; a method taking
     * a {@link StreamObserver} parameter receives replies through that observer.
     *
     * @param invocation the method invocation to send
     * @return future completing with the reply or the request observer
     */
    @Override
    public Future<R> call(Invocation invocation) {
        Object[] values = invocation.arguments().values();
        Type replyMessageType = messageType(invocation);
        StreamObserver<R> responseObserver = responseObserver(values);
        if (StreamObserver.class.isAssignableFrom(replyType().rawType())) {
            return requestObserverFuture(streamCall(responseObserver, invocation, replyMessageType));
        }
        if (responseObserver == null) {
            return unaryCall(invocation, replyMessageType);
        }
        serverStreamCall(responseObserver, invocation, replyMessageType);
        return completed(null);
    }

    /**
     * Executes one unary gRPC call.
     *
     * @param args request arguments
     * @return reply future
     */
    public Future<R> unaryCall(Object... args) {
        return unaryCall(new PositionalInvocation(args), replyType().type());
    }

    /**
     * Executes one server-streaming gRPC call.
     *
     * @param responseObserver reply observer
     * @param args             request arguments
     */
    public void serverStreamCall(StreamObserver<R> responseObserver, Object... args) {
        serverStreamCall(responseObserver, new PositionalInvocation(args), replyType().type());
    }

    /**
     * Executes one client-streaming gRPC call.
     *
     * @param responseObserver reply observer
     * @param args             request arguments
     * @param <T>              request message type
     * @return request observer
     */
    public <T> StreamObserver<T> clientStreamCall(StreamObserver<R> responseObserver, Object... args) {
        return streamCall(responseObserver, new PositionalInvocation(args), replyType().type());
    }

    /**
     * Executes one bidirectional-streaming gRPC call.
     *
     * @param responseObserver reply observer
     * @param args             request arguments
     * @param <T>              request message type
     * @return request observer
     */
    public <T> StreamObserver<T> bidirectionalStreamCall(StreamObserver<R> responseObserver, Object... args) {
        return streamCall(responseObserver, new PositionalInvocation(args), replyType().type());
    }

    private Future<R> unaryCall(Invocation invocation, Type replyMessageType) {
        Promise<R> promise = new Promise<>();
        GrpcClientCall<R> call = openStream(invocation, replyMessageType);
        call.receive(promiseObserver(promise), 1);
        call.send(invocation.arguments().values());
        scheduleDeadline(promise, call);
        return promise;
    }

    private void serverStreamCall(
            StreamObserver<R> responseObserver,
            Invocation invocation,
            Type replyMessageType
    ) {
        GrpcClientCall<R> call = openStream(invocation, replyMessageType);
        call.receive(responseObserver, Long.MAX_VALUE);
        call.send(invocation.arguments().values());
    }

    private <T> StreamObserver<T> streamCall(
            StreamObserver<R> responseObserver,
            Invocation invocation,
            Type replyMessageType
    ) {
        GrpcClientCall<R> call = openStream(invocation, replyMessageType);
        call.receive(responseObserver, Long.MAX_VALUE);
        return call.sender();
    }

    private GrpcClientCall<R> openStream(Invocation invocation, Type replyMessageType) {
        Request request = protocol().createRequest(this, invocation);
        CallContext<Request, Caller<?>> context = new CallContext<>(
                module(),
                request,
                this,
                GrpcStreamMode.INSTANCE,
                invocation.arguments().values()
        );
        Interaction.Result result = callStageChain().proceed(context);
        StreamHandle<GrpcMessage, GrpcMessage> handle = result.excepted();
        return new GrpcClientCall<>(handle, GrpcSupport.serializer(this), replyMessageType);
    }

    private Type messageType(Invocation invocation) {
        if (invocation instanceof MethodInvocation methodInvocation) {
            GrpcMethod contract = new GrpcMethod(methodInvocation.method());
            return contract.observesResponse() ? contract.responseType() : replyType().type();
        }
        return replyType().type();
    }

    @SuppressWarnings("unchecked")
    private StreamObserver<R> responseObserver(Object[] values) {
        for (Object value : values) {
            if (value instanceof StreamObserver<?> observer) {
                return (StreamObserver<R>) observer;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Future<R> requestObserverFuture(StreamObserver<R> responseObserver) {
        Future<?> future = completed(responseObserver);
        return (Future<R>) future;
    }

    private <T> Future<T> completed(T value) {
        Promise<T> promise = new Promise<>();
        promise.success(value);
        return promise;
    }

    private StreamObserver<R> promiseObserver(Promise<R> promise) {
        return new StreamObserver<>() {
            @Override
            public void onNext(R value) {
                promise.success(value);
            }

            @Override
            public void onError(Throwable cause) {
                promise.failure(toRpcException(cause));
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    private void scheduleDeadline(Promise<R> promise, GrpcClientCall<R> call) {
        long timeout = option(TIMEOUT);
        if (timeout < 0) {
            return;
        }
        Scheduler scheduler = platform().singleComponent(Scheduler.class);
        if (scheduler == null) {
            return;
        }
        ScheduledFuture<?> task = scheduler.addDisposable(() -> {
            EffiRpcException failure = PredefinedErrorCode.DEADLINE_EXCEEDED.fail(timeout);
            call.cancel(failure);
            promise.failure(failure);
        }, timeout, TimeUnit.MILLISECONDS);
        promise.onComplete(ignored -> task.cancel(false));
    }

    private EffiRpcException toRpcException(Throwable cause) {
        return cause instanceof EffiRpcException exception
                ? exception
                : TransportErrorCodes.CHANNEL_EXCEPTION.fail(cause, GrpcProtocol.NAME, cause.getMessage());
    }

    /**
     * Builds {@link GrpcCaller} instances.
     *
     * @param <T> reply message type
     */
    public static class Builder<T> extends HttpCaller.Builder<GrpcCaller<T>, Builder<T>> {

        public Builder(TypeCapture<T> returnType) {
            super(GrpcProtocol.VERSION, returnType);
        }

        @Override
        protected GrpcCaller<T> newInstance() {
            return new GrpcCaller<>(this);
        }

        @Override
        protected void validate() {
            if (StringUtil.isBlank(option(SerializationOptions.SERIALIZER))) {
                serializer(ProtobufSerializer.NAME);
            }
            super.validate();
        }
    }
}
