package io.effi.rpc.protocol.grpc.client;

import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcStreamMode;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.stream.StreamHandle;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;
import io.effi.rpc.serialization.protobuf.ProtobufSerializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

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
     * Executes one unary gRPC call.
     *
     * @param args request arguments
     * @return reply future
     */
    public Future<R> unaryCall(Object... args) {
        PositionalInvocation invocation = new PositionalInvocation(args);
        Promise<R> promise = new Promise<>();
        GrpcClientCall<R> call = openStream(invocation);
        call.receive(promiseObserver(promise), 1);
        call.send(invocation.arguments().values());
        scheduleDeadline(promise, call);
        return promise;
    }

    /**
     * Executes one server-streaming gRPC call.
     *
     * @param responseObserver reply observer
     * @param args             request arguments
     */
    public void serverStreamCall(StreamObserver<R> responseObserver, Object... args) {
        PositionalInvocation invocation = new PositionalInvocation(args);
        GrpcClientCall<R> call = openStream(invocation);
        call.receive(responseObserver, Long.MAX_VALUE);
        call.send(invocation.arguments().values());
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
        return streamCall(responseObserver, args);
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
        return streamCall(responseObserver, args);
    }

    private <T> StreamObserver<T> streamCall(StreamObserver<R> responseObserver, Object... args) {
        PositionalInvocation invocation = new PositionalInvocation(args);
        GrpcClientCall<R> call = openStream(invocation);
        call.receive(responseObserver, Long.MAX_VALUE);
        return call.sender();
    }

    private GrpcClientCall<R> openStream(PositionalInvocation invocation) {
        Request request = protocol().createRequest(this, invocation);
        CallContext<Request, Caller<?>> context = new CallContext<>(module(), request, this, GrpcStreamMode.INSTANCE, invocation.arguments().values());
        Interaction.Result result = callStageChain().proceed(context);
        StreamHandle<GrpcMessage, GrpcMessage> handle = result.excepted();
        return new GrpcClientCall<>(handle, GrpcSupport.serializer(this), replyType().type());
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
