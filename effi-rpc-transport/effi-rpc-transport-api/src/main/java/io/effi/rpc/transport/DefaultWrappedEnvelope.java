package io.effi.rpc.transport;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Provides the default implementation of {@link WrappedEnvelope}.
 */
public abstract class DefaultWrappedEnvelope<E extends Envelope, I extends Invoker<?>,
        C extends ExecutorContext<E, I, C>> implements WrappedEnvelope<I, C> {

    protected final C context;

    protected final Channel channel;

    protected E envelope;

    protected DefaultWrappedEnvelope(C context, Channel channel) {
        this.context = AssertUtil.notNull(context, "context");
        this.channel = AssertUtil.notNull(channel, "channel");
        this.envelope = context.envelope();
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

