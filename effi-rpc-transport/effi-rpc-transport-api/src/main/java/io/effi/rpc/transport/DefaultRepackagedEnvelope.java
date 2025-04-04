package io.effi.rpc.transport;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Default implementation of {@link RepackagedEnvelope}.
 *
 * @param <I> the type of the invoker
 * @param <E> the type of the envelope
 * @param <C> the type of the executor context
 */
public abstract class DefaultRepackagedEnvelope<E extends Envelope, I extends Invoker<?>,
        C extends ExecutorContext<E, I, C>> implements RepackagedEnvelope<I, C> {

    protected final C context;

    protected final Channel channel;

    protected DefaultRepackagedEnvelope(C context, Channel channel) {
        this.context = AssertUtil.notNull(context, "context");
        this.channel = AssertUtil.notNull(channel, "channel");
    }

    @Override
    public C context() {
        return context;
    }

    @Override
    public Channel channel() {
        return channel;
    }
}

