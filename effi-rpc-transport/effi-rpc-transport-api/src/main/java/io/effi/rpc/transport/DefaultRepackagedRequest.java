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

    private Envelope.Request request;

    public DefaultRepackagedRequest(InvocationContext<Envelope.Request, I> context, Channel channel) {
        super(context, channel);
        this.request = context.source();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedRequest<I> encode() {
        if (request.isInstance() && context.invoker() instanceof Caller<?>) {
            request = channel.protocol()
                    .clientCodec().encode((RepackagedRequest<Caller<?>>) this);
        }
        return this;
    }

    @Override
    public Envelope.Request request() {
        return request;
    }
}


