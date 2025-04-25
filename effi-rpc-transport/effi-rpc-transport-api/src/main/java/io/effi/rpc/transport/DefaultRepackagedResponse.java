package io.effi.rpc.transport;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Default implementation of {@link RepackagedResponse}.
 *
 * @param <I> the type of the invoker
 */
public class DefaultRepackagedResponse<I extends Invoker<?>>
        extends DefaultRepackagedEnvelope<Envelope.Response, I, ReplyContext<Envelope.Response, I>>
        implements RepackagedResponse<I> {

    public DefaultRepackagedResponse(ReplyContext<Envelope.Response, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedResponse<I> encode() {
        if (envelope.isInstance() && context.invoker() instanceof Callee<?>) {
            envelope = channel.protocol()
                    .serverCodec().encode((RepackagedResponse<Callee<?>>) this);
        }
        return this;
    }

}


