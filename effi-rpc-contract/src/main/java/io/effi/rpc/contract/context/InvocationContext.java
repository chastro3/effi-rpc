package io.effi.rpc.contract.context;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Context for an RPC invocation.
 *
 * @param <T> the type of the request
 * @param <I> the type of the invoker
 */
public class InvocationContext<T extends Envelope.Request, I extends Invoker<?>>
        extends ExecutorContext<T, I, InvocationContext<T, I>> {

    private final Object[] args;

    public InvocationContext(EffiRpcModule module, T request, I invoker, Object[] args) {
        super(module, request, invoker);
        this.args = args;
    }

    /**
     * Returns the arguments for the invocation.
     */
    public Object[] args() {
        return args;
    }
}

