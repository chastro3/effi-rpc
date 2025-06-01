package io.effi.rpc.transport;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides the default implementation of {@link WrappedResponse}.
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


