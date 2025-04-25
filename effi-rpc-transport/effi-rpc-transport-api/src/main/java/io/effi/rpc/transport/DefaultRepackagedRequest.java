package io.effi.rpc.transport;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Default implementation of {@link RepackagedRequest}.
 *
 * @param <I> the type of the invoker
 */
public class DefaultRepackagedRequest<I extends Invoker<?>>
        extends DefaultRepackagedEnvelope<Envelope.Request, I, InvocationContext<Envelope.Request, I>>
        implements RepackagedRequest<I> {

    public DefaultRepackagedRequest(InvocationContext<Envelope.Request, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedRequest<I> encode() {
        if (envelope.isInstance() && context.invoker() instanceof Caller<?>) {
            envelope = channel.protocol()
                    .clientCodec().encode((RepackagedRequest<Caller<?>>) this);
        }
        return this;
    }
}


