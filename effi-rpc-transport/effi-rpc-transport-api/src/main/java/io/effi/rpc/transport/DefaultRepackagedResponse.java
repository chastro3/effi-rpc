package io.effi.rpc.transport;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.transport.endpoint.Channel;

public class DefaultRepackagedResponse<I extends Invoker<?>>
        extends DefaultRepackagedEnvelope<Envelope.Response, I, ReplyContext<Envelope.Response, I>>
        implements RepackagedResponse<I> {

    private Envelope.Response response;

    public DefaultRepackagedResponse(ReplyContext<Envelope.Response, I> context, Channel channel) {
        super(context, channel);
        this.response = context.source();
    }

    @SuppressWarnings("unchecked")
    @Override
    public RepackagedResponse<I> encode() {
        if (response.isInstance() && context.invoker() instanceof Callee<?>) {
            response = channel.protocol()
                    .serverCodec().encode((RepackagedResponse<Callee<?>>) this);
        }
        return this;
    }

    @Override
    public Envelope.Response response() {
        return response;
    }

}


