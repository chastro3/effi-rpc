package io.effi.rpc.protocol.grpc.stream;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.MessagePublisher;
import io.effi.rpc.concurrent.MessageSubscriber;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.protocol.grpc.codec.GrpcFraming;
import io.effi.rpc.protocol.grpc.codec.GrpcMessage;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.util.AssertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides the shared gRPC stream state machine over one HTTP/2 stream channel.
 */
public abstract class GrpcStream implements MessagePublisher<GrpcMessage>, MessageSubscriber<GrpcMessage> {

    private final Channel channel;

    private final SubmissionPublisher<GrpcMessage> inbound;

    private final List<Runnable> readyCallbacks = new CopyOnWriteArrayList<>();

    private final AtomicBoolean outboundCompleted = new AtomicBoolean(false);

    private final AtomicBoolean terminal = new AtomicBoolean(false);

    protected GrpcStream(Channel channel) {
        this.channel = AssertUtil.notNull(channel, "channel");
        this.inbound = new SubmissionPublisher<>(channel.eventLoop(), Flow.defaultBufferSize());
    }

    @Override
    public void subscribe(Flow.Subscriber<? super GrpcMessage> subscriber) {
        inbound.subscribe(AssertUtil.notNull(subscriber, "subscriber"));
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(GrpcMessage message) {
        AssertUtil.notNull(message, "message");
        send(message).onComplete(result -> {
            if (result.failed()) {
                fail(result.cause());
            }
        });
    }

    @Override
    public void onError(Throwable cause) {
        fail(cause instanceof EffiRpcException exception
                ? exception
                : TransportErrorCodes.CHANNEL_WRITE.fail(cause, channel.remoteAddress()));
    }

    @Override
    public void onComplete() {
        if (outboundCompleted.compareAndSet(false, true)) {
            writeCompletion();
        }
    }

    @Override
    public boolean ready() {
        return !terminal.get() && channel.isWritable();
    }

    @Override
    public void onReady(Runnable callback) {
        readyCallbacks.add(AssertUtil.notNull(callback, "callback"));
    }

    @Override
    public void cancel(EffiRpcException reason) {
        fail(reason);
    }

    /**
     * Delivers one inbound message to the publisher.
     *
     * @param message inbound message
     */
    public void inboundMessage(GrpcMessage message) {
        if (terminal.get() || inbound.isClosed()) {
            return;
        }
        if (inbound.offer(message, (subscriber, item) -> false) < 0) {
            fail(TransportErrorCodes.CHANNEL_OVERLOADED.fail(channel));
        }
    }

    /**
     * Completes the inbound publisher.
     */
    public void inboundCompleted() {
        if (terminal.compareAndSet(false, true)) {
            inbound.close();
        }
    }

    /**
     * Terminates the inbound publisher with the supplied failure.
     *
     * @param cause terminal failure
     */
    public void inboundError(EffiRpcException cause) {
        if (terminal.compareAndSet(false, true)) {
            inbound.closeExceptionally(cause);
        }
    }

    /**
     * Notifies pending callbacks when the channel becomes writable.
     */
    public void writabilityChanged() {
        if (ready()) {
            readyCallbacks.forEach(Runnable::run);
        }
    }

    /**
     * Writes one framed message through this stream.
     *
     * @param message outbound message
     * @return write completion future
     */
    public Future<Void> send(GrpcMessage message) {
        Promise<Void> promise = new Promise<>();
        if (terminal.get() || !channel.isActive()) {
            promise.failure(TransportErrorCodes.CHANNEL_INACTIVE.fail(channel));
            return promise;
        }
        if (!channel.isWritable()) {
            promise.failure(TransportErrorCodes.CHANNEL_OVERLOADED.fail(channel));
            return promise;
        }
        ByteBuf payload = GrpcFraming.encode(channel.alloc(), message.compressed(), message.payload());
        channel.writeAndFlush(new DefaultHttp2DataFrame(payload, false)).addListener(result -> {
            if (result.isSuccess()) {
                promise.success(null);
            } else {
                promise.failure(TransportErrorCodes.CHANNEL_WRITE.fail(result.cause(), channel.remoteAddress()));
            }
        });
        return promise;
    }

    /**
     * Terminates the stream with the supplied failure.
     *
     * @param cause terminal failure
     */
    public void fail(EffiRpcException cause) {
        AssertUtil.notNull(cause, "cause");
        if (outboundCompleted.compareAndSet(false, true)) {
            writeFailure(cause);
        }
        inboundError(cause);
    }

    /**
     * Returns the underlying HTTP/2 stream channel.
     */
    protected Channel channel() {
        return channel;
    }

    /**
     * Writes the side-specific stream completion.
     */
    protected abstract void writeCompletion();

    /**
     * Writes the side-specific terminal failure.
     *
     * @param cause terminal failure
     */
    protected abstract void writeFailure(EffiRpcException cause);
}
