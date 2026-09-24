package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.message.EncodableOutputMessage;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the transport resources for one RPC call attempt.
 * <p>
 * The state and current resource are updated atomically so timeout, acquire completion,
 * write failure, and response completion cannot release the same resource twice.
 */
final class CallAttempt {

    private static final Logger logger = LoggerFactory.getLogger(CallAttempt.class);

    private final ReplyFuture future;

    private final Client client;

    private final TransportProtocol protocol;

    private final CallContext<Request, Caller<?>> context;

    private final AtomicReference<State> state = new AtomicReference<>(IdleState.INSTANCE);

    CallAttempt(
            ReplyFuture future,
            Client client,
            TransportProtocol protocol,
            CallContext<Request, Caller<?>> context
    ) {
        this.future = future;
        this.client = client;
        this.protocol = protocol;
        this.context = context;
        future.onCancel(reason -> cancel());
        future.onComplete(result -> finish());
    }

    void start() {
        if (!(state.get() instanceof IdleState)) {
            return;
        }

        Future<? extends Channel> acquire;
        try {
            acquire = client.fetchChannel();
        } catch (Throwable e) {
            if (state.compareAndSet(IdleState.INSTANCE, DoneState.INSTANCE)) {
                future.failure(fetchFailure(e));
            }
            return;
        }
        if (acquire == null) {
            if (state.compareAndSet(IdleState.INSTANCE, DoneState.INSTANCE)) {
                future.failure(fetchFailure(new IllegalStateException("Client returned a null channel future")));
            }
            return;
        }

        AcquiringState acquiring = new AcquiringState(acquire);
        if (!state.compareAndSet(IdleState.INSTANCE, acquiring)) {
            acquire.cancel(PredefinedErrorCode.CALL_CANCELLED.fail("call attempt cancelled"));
            return;
        }
        acquire.onComplete(result -> onChannelAcquired(acquiring, result));
    }

    private void onChannelAcquired(AcquiringState acquiring, Result<? extends Channel> result) {
        if (result.failed()) {
            if (state.compareAndSet(acquiring, DoneState.INSTANCE)) {
                future.failure(fetchFailure(result.cause()));
            }
            return;
        }

        Channel channel = result.value();
        if (channel == null) {
            if (state.compareAndSet(acquiring, DoneState.INSTANCE)) {
                future.failure(fetchFailure(new IllegalStateException("Channel acquisition returned null")));
            }
            return;
        }

        if (!state.compareAndSet(acquiring, new ActiveState(channel))) {
            closeQuietly(channel);
            return;
        }
        if (future.completed()) {
            cancel();
            return;
        }
        send(channel);
    }

    private void send(Channel channel) {
        try {
            var outputMessage = EncodableOutputMessage.create(context, channel, protocol.clientCodec());
            channel.send(outputMessage).onComplete(result -> {
                if (result.failed()) {
                    handleWriteFailure(channel, result.cause());
                }
            });
        } catch (Throwable e) {
            handleWriteFailure(channel, e);
        }
    }

    private void handleWriteFailure(Channel channel, Throwable cause) {
        State previous = transition(DoneState.INSTANCE);
        if (previous instanceof ActiveState) {
            closeQuietly(channel);
        }
        future.failure(writeFailure(cause));
    }

    private void cancel() {
        State previous = transition(CancelledState.INSTANCE);
        if (previous instanceof AcquiringState acquiring) {
            acquiring.acquire().cancel(PredefinedErrorCode.CALL_CANCELLED.fail("call attempt cancelled"));
        } else if (previous instanceof ActiveState active) {
            closeQuietly(active.channel());
        }
    }

    private void finish() {
        State previous = transition(DoneState.INSTANCE);
        if (previous instanceof AcquiringState acquiring) {
            acquiring.acquire().cancel(PredefinedErrorCode.CALL_CANCELLED.fail("call attempt cancelled"));
        }
    }

    private State transition(State target) {
        while (true) {
            State current = state.get();
            if (current == DoneState.INSTANCE || current == CancelledState.INSTANCE) {
                return current;
            }
            if (state.compareAndSet(current, target)) {
                return current;
            }
        }
    }

    private EffiRpcException fetchFailure(Throwable cause) {
        SmartURL url = context.message().url();
        return TransportErrorCodes.FETCH_CHANNEL.fail(cause, url.host(), url.scheme());
    }

    private EffiRpcException writeFailure(Throwable cause) {
        return TransportErrorCodes.CHANNEL_WRITE.fail(cause, context.message().url().host());
    }

    private void closeQuietly(Channel channel) {
        try {
            channel.close();
        } catch (Throwable e) {
            logger.warn("Failed to close transport channel '{}'", e, channel);
        }
    }

    private sealed interface State permits IdleState, AcquiringState, ActiveState, DoneState, CancelledState {
    }

    private enum IdleState implements State {
        INSTANCE
    }

    private record AcquiringState(Future<? extends Channel> acquire) implements State {
    }

    private record ActiveState(Channel channel) implements State {
    }

    private enum DoneState implements State {
        INSTANCE
    }

    private enum CancelledState implements State {
        INSTANCE
    }
}
