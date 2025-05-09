package io.effi.rpc.transport;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Default implementation of {@link WrappedResponse}.
 *
 * @param <I> the type of the invoker
 */
public class DefaultWrappedResponse<I extends Invoker<?>>
        extends DefaultWrappedEnvelope<Envelope.Response, I, ReplyContext<Envelope.Response, I>>
        implements WrappedResponse<I> {

    public DefaultWrappedResponse(ReplyContext<Envelope.Response, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedResponse<I> encode() {
        if (envelope.isInstance() && context.invoker() instanceof Callee<?>) {
            envelope = channel.protocol()
                    .serverCodec().encode((WrappedResponse<Callee<?>>) this);
        }
        return this;
    }

}


