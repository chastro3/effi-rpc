package io.effi.rpc.transport;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.ExecutorContext;

/**
 * Wrapper for an {@link Envelope}, providing access to
 * the associated execution context.
 *
 * @param <E> the type of the envelope (request or response)
 * @param <I> the type of the invoker
 * @param <C> the type of the executor context
 */
public abstract class EnvelopeWrapper<E extends Envelope, I extends Invoker<?>, C
        extends ExecutorContext<E, I, C>> {

    protected final C context;

    protected EnvelopeWrapper(C context) {
        this.context = AssertUtil.notNull(context, "context");
    }

    /**
     * Returns the associated execution context.
     *
     * @return the execution context
     */
    public C context() {
        return context;
    }
}

