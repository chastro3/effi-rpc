package io.effi.rpc.contract.context;

import io.effi.rpc.contract.*;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.util.AbstractAttributes;

import java.util.function.Supplier;

/**
 * Represents the context for executing an RPC invocation.
 *
 * @see InvocationContext
 * @see ReplyContext
 */
public abstract class ExecutorContext<T extends Envelope, I extends Invoker<?>, C extends ExecutorContext<T, I, C>>
        extends AbstractAttributes implements ModuleSource {

    private final EffiRpcModule module;

    private final I invoker;

    private final Envelope envelope;

    protected Supplier<Result> executor;

    protected ExecutorContext(EffiRpcModule module, T envelope, I invoker) {
        this.module = module;
        this.envelope = envelope;
        this.invoker = invoker;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    public Supplier<Result> executor() {
        return executor;
    }

    @SuppressWarnings("unchecked")
    public T envelope() {
        return (T) envelope;
    }

    public I invoker() {
        return invoker;
    }

    @SuppressWarnings("unchecked")
    public C executor(Supplier<Result> executor) {
        this.executor = executor;
        return (C) this;
    }

    /**
     * Executes the invocation and returns the result.
     */
    public Result execute() {
        if (executor == null) return ResultType.VALUE.createResult(envelope.url(), null);
        return executor.get();
    }

    /**
     * Checks if the context is server-side.
     */
    public boolean isServerSide() {
        return invoker instanceof Callee<?>;
    }

    /**
     * Checks if the context is client-side.
     */
    public boolean isClientSide() {
        return invoker instanceof Caller<?>;
    }
}

