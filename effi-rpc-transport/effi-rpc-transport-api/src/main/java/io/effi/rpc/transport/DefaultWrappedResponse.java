package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides the default implementation of {@link WrappedResponse}.
 */
public class DefaultWrappedResponse<I extends CallSide>
        extends DefaultWrappedEnvelope<Message.Response, I, ReplyContext<Message.Response, I>>
        implements WrappedResponse<I> {

    public DefaultWrappedResponse(ReplyContext<Message.Response, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedResponse<I> encode() {
        if (envelope.isInstance() && context.callSide() instanceof Callee) {
            envelope = channel.protocol()
                    .serverCodec().encode((WrappedResponse<Callee>) this);
        }
        return this;
    }

}


