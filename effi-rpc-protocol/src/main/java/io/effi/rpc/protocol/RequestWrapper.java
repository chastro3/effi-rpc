package io.effi.rpc.protocol;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.protocol.codec.ClientCodec;

/**
 * Wrapper for a {@link Envelope.Request}, providing access
 * to the associated invocation context. This class allows encoding
 * requests using a {@link ClientCodec} and offers a convenient way
 * to access and manipulate the underlying request.
 *
 * @param <I> the type of the invoker (e.g., {@link Caller} or {@link Callee})
 */
public class RequestWrapper<I extends Invoker<?>>
        extends EnvelopeWrapper<Envelope.Request, I, InvocationContext<Envelope.Request, I>> {

    private final ClientCodec clientCodec;  // Codec used to encode the request

    private Envelope.Request request;       // The actual request being wrapped

    /**
     * Constructs a {@link RequestWrapper} with the provided invocation context
     * and optional {@link ClientCodec} for encoding the request.
     *
     * @param context     the invocation context containing the request and invoker
     * @param clientCodec the codec used for encoding the request, or null
     */
    public RequestWrapper(InvocationContext<Envelope.Request, I> context, ClientCodec clientCodec) {
        super(context);
        this.request = context.source();
        this.clientCodec = clientCodec;
    }

    /**
     * Static factory method to create a {@link RequestWrapper} from an invocation context
     * for a {@link Callee}.
     *
     * @param context the invocation context
     * @return a new instance of {@link RequestWrapper}
     */
    public static RequestWrapper<Callee<?>> parse(InvocationContext<Envelope.Request, Callee<?>> context) {
        return new RequestWrapper<>(context, null);
    }

    /**
     * Encodes the request using the {@link ClientCodec} and a {@link NettyChannel}.
     * If the context's invoker is a {@link Caller}, the request is encoded.
     *
     * @param channel the {@link NettyChannel} used for encoding
     * @return the current {@link RequestWrapper} instance for chaining
     */
    @SuppressWarnings("unchecked")
    public RequestWrapper<I> encode(NettyChannel channel) {
        if (clientCodec != null && context.invoker() instanceof Caller<?>) {
            request = clientCodec.encode(channel, (RequestWrapper<Caller<?>>) this);
        }
        return this;
    }

    /**
     * Returns the request wrapped by this {@link RequestWrapper}.
     *
     * @return the request envelope
     */
    public Envelope.Request request() {
        return request;
    }
}


