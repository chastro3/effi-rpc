package io.effi.rpc.boot.call;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.ChannelCallBindings;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.message.EncodableOutputMessage;
import io.effi.rpc.util.AssertUtil;

import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;

/**
 * Owns the transport resources of one client call attempt.
 * <p>
 * Acquires a channel, binds the attempt to it, and sends the request. Retry and deadline
 * policy belong to the call execution that creates the attempt. The state and current
 * resource are updated atomically so cancellation, acquisition, write failure, and
 * completion cannot release the same resource twice.
 */
public final class CallAttempt {

    private static final Logger logger = LoggerFactory.getLogger(CallAttempt.class);

    private final ReplyFuture replyFuture;

    private final Client client;

    private final TransportProtocol protocol;

    private final ChannelCallBindings channelBindings;

    private final AtomicReference<State> state = new AtomicReference<>(Idle.INSTANCE);

    public CallAttempt(ReplyFuture replyFuture, Client client, TransportProtocol protocol) {
        this.replyFuture = replyFuture;
        this.client = client;
        this.protocol = protocol;
        this.channelBindings = AssertUtil.notNull(
                replyFuture.context().platform().singleComponent(ChannelCallBindings.class),
                "channel call bindings"
        );
        replyFuture.onCancel(reason -> cancel());
        replyFuture.onComplete(result -> release());
    }

    public void dispatch() {
        if (!(state.get() instanceof Idle)) {
            return;
        }

        Future<? extends Channel> acquire;
        try {
            acquire = client.fetchChannel();
        } catch (Throwable cause) {
            failAcquisition(Idle.INSTANCE, cause);
            return;
        }
        if (acquire == null) {
            failAcquisition(Idle.INSTANCE, new IllegalStateException("Client returned a null channel future"));
            return;
        }

        Acquiring acquiring = new Acquiring(acquire);
        if (!state.compareAndSet(Idle.INSTANCE, acquiring)) {
            cancelAcquisition(acquiring);
            return;
        }
        acquire.onComplete(result -> onChannelAcquired(acquiring, result));
    }

    private void onChannelAcquired(Acquiring acquiring, Result<? extends Channel> result) {
        if (result.failed()) {
            failAcquisition(acquiring, result.cause());
            return;
        }

        Channel channel = result.value();
        if (channel == null) {
            failAcquisition(acquiring, new IllegalStateException("Channel acquisition returned null"));
            return;
        }

        if (!state.compareAndSet(acquiring, new Active(channel))) {
            closeQuietly(channel);
            return;
        }
        if (replyFuture.completed()) {
            cancel();
            return;
        }
        if (!channelBindings.bind(replyFuture.id(), channel)) {
            closeQuietly(channel);
            if (!replyFuture.completed()) {
                replyFuture.failure(retryable(TransportErrorCodes.CHANNEL_INACTIVE.fail(channel)));
            }
            return;
        }
        sendRequest(channel);
    }

    private void sendRequest(Channel channel) {
        try {
            var outputMessage = EncodableOutputMessage.create(
                    replyFuture.context(),
                    channel,
                    protocol.clientCodec()
            );
            channel.send(outputMessage).onComplete(result -> {
                if (result.failed()) {
                    onSendFailed(channel, result.cause());
                }
            });
        } catch (Throwable e) {
            onSendFailed(channel, e);
        }
    }

    private void onSendFailed(Channel channel, Throwable cause) {
        if (!(transition(Done.INSTANCE) instanceof Active active)) {
            return;
        }
        channelBindings.unbind(replyFuture.id(), active.channel());
        closeQuietly(active.channel());
        replyFuture.failure(writeFailure(cause));
    }

    private void cancel() {
        State previous = transition(Cancelled.INSTANCE);
        if (previous instanceof Acquiring acquiring) {
            cancelAcquisition(acquiring);
        } else if (previous instanceof Active active) {
            closeQuietly(active.channel());
        }
    }

    private void release() {
        State previous = transition(Done.INSTANCE);
        if (previous instanceof Acquiring acquiring) {
            cancelAcquisition(acquiring);
        } else if (previous instanceof Active active) {
            channelBindings.unbind(replyFuture.id(), active.channel());
        }
    }

    private void failAcquisition(State expected, Throwable cause) {
        if (state.compareAndSet(expected, Done.INSTANCE)) {
            replyFuture.failure(acquisitionFailure(cause));
        }
    }

    private static void cancelAcquisition(Acquiring acquiring) {
        acquiring.channelFuture().cancel(PredefinedErrorCode.CALL_CANCELLED.fail("call attempt cancelled"));
    }

    private State transition(State target) {
        while (true) {
            State current = state.get();
            if (current == Done.INSTANCE || current == Cancelled.INSTANCE) {
                return current;
            }
            if (state.compareAndSet(current, target)) {
                return current;
            }
        }
    }

    private EffiRpcException acquisitionFailure(Throwable cause) {
        SmartURL url = replyFuture.context().message().url();
        return retryable(TransportErrorCodes.FETCH_CHANNEL.fail(cause, url.host(), url.scheme()));
    }

    private EffiRpcException writeFailure(Throwable cause) {
        return TransportErrorCodes.CHANNEL_WRITE.fail(cause, replyFuture.context().message().url().host());
    }

    private static EffiRpcException retryable(EffiRpcException cause) {
        return cause.withMetadata(Map.of(KeyConstant.RETRYABLE, Boolean.TRUE.toString()));
    }

    private void closeQuietly(Channel channel) {
        try {
            channel.close();
        } catch (Throwable e) {
            logger.warn("Failed to close transport channel '{}'", e, channel);
        }
    }

    private sealed interface State permits Idle, Acquiring, Active, Done, Cancelled {
    }

    private enum Idle implements State {
        INSTANCE
    }

    private record Acquiring(Future<? extends Channel> channelFuture) implements State {
    }

    private record Active(Channel channel) implements State {
    }

    private enum Done implements State {
        INSTANCE
    }

    private enum Cancelled implements State {
        INSTANCE
    }
}
