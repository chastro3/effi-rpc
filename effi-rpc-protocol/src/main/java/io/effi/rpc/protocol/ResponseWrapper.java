package io.effi.rpc.protocol;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.contract.*;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.protocol.codec.ServerCodec;

/**
 * Wrapper for an {@link Envelope.Response}, providing access to
 * the associated reply context and result. This class allows encoding
 * responses using a {@link ServerCodec} and offers a convenient way
 * to access and manipulate the underlying response and result.
 *
 * @param <I> the type of the invoker (e.g., {@link Caller} or {@link Callee})
 */
public class ResponseWrapper<I extends Invoker<?>>
        extends EnvelopeWrapper<Envelope.Response, I, ReplyContext<Envelope.Response, I>> {

    private final ServerCodec serverCodec;

    private Envelope.Response response;

    /**
     * Constructs a {@link ResponseWrapper} with the provided reply context,
     * result of the RPC operation, and an optional {@link ServerCodec} for encoding
     * the response.
     *
     * @param context     the reply context containing the response and invoker
     * @param serverCodec the codec used for encoding the response, or null
     */
    public ResponseWrapper(ReplyContext<Envelope.Response, I> context, ServerCodec serverCodec) {
        super(context);
        this.response = context.source();
        this.serverCodec = serverCodec;
    }

    /**
     * Static factory method to parameter a response and return a corresponding {@link ResponseWrapper}.
     * <p>
     * This method verifies if the response is successfully decoded before wrapping it. If not,
     * it throws an {@link IllegalArgumentException}.
     *
     * @param context the reply context containing the response and invoker
     * @return a {@link ResponseWrapper} if valid, or throws an exception if the response is invalid
     * @throws IllegalArgumentException if the response has not been decoded
     */
    public static ResponseWrapper<Caller<?>> parse(ReplyContext<Envelope.Response, Caller<?>> context) {
        // Ensure the response has been decoded
        if (!context.source().isInstance()) {
            throw new IllegalArgumentException("The current response has not been decoded");
        }
        return new ResponseWrapper<>(context, null);
    }

    /**
     * Encodes the response using the {@link ServerCodec} and a {@link NettyChannel}.
     * If the context's invoker is of type {@link Callee}, the response is encoded.
     *
     * @param channel the {@link NettyChannel} used for encoding
     * @return the current {@link ResponseWrapper} instance for chaining
     */
    @SuppressWarnings("unchecked")
    public ResponseWrapper<I> encode(NettyChannel channel) {
        if (serverCodec != null && context.invoker() instanceof Callee<?>) {
            response = serverCodec.encode(channel, (ResponseWrapper<Callee<?>>) this);
        }
        return this;
    }

    /**
     * Returns the response wrapped by this {@link ResponseWrapper}.
     *
     * @return the response envelope
     */
    public Envelope.Response response() {
        return response;
    }

    /**
     * Checks if the response indicates success.
     *
     * @return true if the response is successful, false otherwise
     */
    public boolean isSuccess() {
        return response().isSuccess();
    }

    /**
     * Returns the result of the RPC operation.
     *
     * @return the result
     */
    public Result result() {
        return context.result();
    }

    /**
     * Returns the cause of failure, if any, in the response.
     *
     * @return the {@link EffiRpcException} representing the cause of failure, or null if success
     */
    public EffiRpcException cause() {
        return context.result().exception();
    }
}


