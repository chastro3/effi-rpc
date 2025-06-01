package io.effi.rpc.transport;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides the default implementation of {@link WrappedRequest}.
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


