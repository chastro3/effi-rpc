package io.effi.rpc.transport;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Default implementation of {@link WrappedRequest}.
 *
 * @param <I> the type of the invoker
 */
public class DefaultWrappedRequest<I extends Invoker<?>>
        extends DefaultWrappedEnvelope<Envelope.Request, I, InvocationContext<Envelope.Request, I>>
        implements WrappedRequest<I> {

    public DefaultWrappedRequest(InvocationContext<Envelope.Request, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedRequest<I> encode() {
        if (envelope.isInstance() && context.invoker() instanceof Caller<?>) {
            envelope = channel.protocol()
                    .clientCodec().encode((WrappedRequest<Caller<?>>) this);
        }
        return this;
    }
}


