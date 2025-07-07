package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides the default implementation of {@link WrappedRequest}.
 */
public class DefaultWrappedRequest<I extends CallSide>
        extends DefaultWrappedEnvelope<Message.Request, I, CallContext<Message.Request, I>>
        implements WrappedRequest<I> {

    public DefaultWrappedRequest(CallContext<Message.Request, I> context, Channel channel) {
        super(context, channel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedRequest<I> encode() {
        if (envelope.isInstance() && context.callSide() instanceof Caller<?>) {
            envelope = channel.protocol()
                    .clientCodec().encode((WrappedRequest<Caller<?>>) this);
        }
        return this;
    }
}


