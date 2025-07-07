package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.ExchangeContext;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides the default implementation of {@link WrappedEnvelope}.
 */
public abstract class DefaultWrappedEnvelope<E extends Message, I extends CallSide,
        C extends ExchangeContext<E, I>> implements WrappedEnvelope<I, C> {

    protected final C context;

    protected final Channel channel;

    protected E envelope;

    protected DefaultWrappedEnvelope(C context, Channel channel) {
        this.context = AssertUtil.notNull(context, "context");
        this.channel = AssertUtil.notNull(channel, "channel");
        this.envelope = context.message();
    }

    @Override
    public C context() {
        return context;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public E envelope() {
        return envelope;
    }
}

