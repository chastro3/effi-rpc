package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.metrics.ServantMetrics;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.http.HttpOptions;
import io.effi.rpc.protocol.grpc.GrpcProtocol;
import io.effi.rpc.protocol.grpc.GrpcStreamMode;
import io.effi.rpc.protocol.grpc.GrpcSupport;
import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.protocol.grpc.stream.GrpcServerStream;
import io.effi.rpc.protocol.grpc.stream.StreamObserver;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2ResetFrame;

/**
 * Handles one server-side gRPC call over its HTTP/2 stream.
 */
public final class GrpcServerHandler extends ChannelInboundHandlerAdapter {

    private final GrpcFraming.Decoder decoder;

    private final Servant servant;

    private final GrpcMethod method;

    private final Serializer serializer;

    private final GrpcServerStream stream;

    private final Request request;

    private OutboundStreamObserver<Object> responseObserver;

    private StreamObserver<Object> requestObserver;

    private boolean invoked;

    GrpcServerHandler(GrpcServer server, GrpcServant servant, Serializer serializer, Channel channel, String path) {
        this.decoder = new GrpcFraming.Decoder(server.config().option(HttpOptions.MAX_MESSAGE_SIZE));
        this.servant = servant;
        this.method = servant.grpcMethod();
        this.serializer = serializer;
        this.stream = new GrpcServerStream(channel);
        this.request = new GrpcRequest(SmartURL.builder().scheme(GrpcProtocol.NAME).path(path).build());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        stream.start();
        if (method.observesResponse()) {
            responseObserver = new OutboundStreamObserver<>(stream, serializer);
        }
        if (method.invokesAtHeaders()) {
            invoked = true;
            requestObserver = invokeObserver();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2DataFrame dataFrame) {
            handleData(dataFrame);
            return;
        }
        if (msg instanceof Http2ResetFrame) {
            fail(TransportErrorCodes.CHANNEL_INACTIVE.fail(ctx.channel()));
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        stream.writabilityChanged();
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        fail(TransportErrorCodes.CHANNEL_INACTIVE.fail(ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        fail(TransportErrorCodes.CHANNEL_EXCEPTION.fail(
                cause,
                ctx.channel().remoteAddress(),
                cause.getMessage()
        ));
        ctx.close();
    }

    private void handleData(Http2DataFrame frame) {
        try {
            for (GrpcMessage message : decoder.decode(frame.content())) {
                handleMessage(message);
            }
        } finally {
            frame.release();
        }
        if (frame.isEndStream()) {
            endStream();
        }
    }

    private void handleMessage(GrpcMessage message) {
        Object value = GrpcSupport.deserialize(serializer, message.payload(), method.requestType());
        if (requestObserver != null) {
            requestObserver.onNext(value);
            return;
        }
        if (invoked) {
            stream.inboundError(InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(
                    "gRPC method expects exactly one request message"
            ));
            return;
        }
        invoked = true;
        invokeRequest(value);
    }

    private void endStream() {
        if (requestObserver != null) {
            requestObserver.onCompleted();
            return;
        }
        if (!invoked) {
            stream.inboundError(InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(
                    "gRPC method requires a request message"
            ));
        }
    }

    private StreamObserver<Object> invokeObserver() {
        Interaction.Result result = invoke(method.arguments(null, responseObserver));
        if (result.failed()) {
            stream.fail(result.cause());
            return null;
        }
        return result.excepted();
    }

    private void invokeRequest(Object message) {
        servant.threadPool().execute(() -> dispatch(message)).onComplete(result -> {
            if (result.failed()) {
                ServantMetrics.of(servant).recordOverload();
                stream.fail(InteractionErrorCodes.SERVER_OVERLOADED.fail(result.cause(), servant.id()));
            }
        });
    }

    private void dispatch(Object message) {
        try {
            Interaction.Result result = invoke(method.arguments(message, responseObserver));
            if (result.failed()) {
                stream.fail(result.cause());
                return;
            }
            if (responseObserver != null) {
                if (!responseObserver.completed()) {
                    responseObserver.onCompleted();
                }
                return;
            }
            stream.onNext(new GrpcMessage(false, GrpcSupport.serialize(serializer, result.value())));
            stream.onComplete();
        } catch (Throwable cause) {
            stream.fail(cause instanceof EffiRpcException exception
                    ? exception
                    : InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(cause, servant.id()));
        }
    }

    private Interaction.Result invoke(Object[] arguments) {
        CallContext<Request, Servant> context = new CallContext<>(
                servant.module(),
                request,
                servant,
                GrpcStreamMode.INSTANCE,
                arguments
        );
        return servant.callStageChain().proceed(context);
    }

    private void fail(EffiRpcException cause) {
        stream.fail(cause);
    }
}
