package io.effi.rpc.contract.context;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Represents the context for an RPC invocation.
 * <p>
 * For the client, occurs before the request is sent.
 * For the server, occurs before the target method is invoked.
 * </p>
 */
public class InvocationContext<T extends Envelope.Request, I extends Invoker<?>>
        extends ExecutorContext<T, I, InvocationContext<T, I>> {

    private final Object[] args;

    public InvocationContext(EffiRpcModule module, T request, I invoker, Object[] args) {
        super(module, request, invoker);
        this.args = args;
    }

    public Object[] args() {
        return args;
    }
}

