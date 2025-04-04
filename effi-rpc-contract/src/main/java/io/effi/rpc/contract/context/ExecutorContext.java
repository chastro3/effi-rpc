package io.effi.rpc.contract.context;

import io.effi.rpc.common.util.AbstractAttributes;
import io.effi.rpc.contract.*;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleSource;

import java.util.function.Supplier;

/**
 * Represents the context for executing an RPC invocation.
 * <p>
 * Provides control over execution through {@link #executor(Supplier)} and {@link #execute()}.
 * </p>
 *
 * @param <T> the type of the source (request/response)
 * @param <I> the type of the invoker
 * @param <C> the type of the concrete subclass of ExecutorContext
 * @see InvocationContext
 * @see ReplyContext
 */
public abstract class ExecutorContext<T extends Envelope, I extends Invoker<?>, C extends ExecutorContext<T, I, C>>
        extends AbstractAttributes implements ModuleSource {

    private final EffiRpcModule module;

    private final I invoker;

    private final Envelope source;

    protected Supplier<Result> executor;

    protected ExecutorContext(EffiRpcModule module, T source, I invoker) {
        this.module = module;
        this.source = source;
        this.invoker = invoker;
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    /**
     * Returns the executor.
     */
    public Supplier<Result> executor() {
        return executor;
    }

    /**
     * Returns the source of the invocation.
     */
    @SuppressWarnings("unchecked")
    public T source() {
        return (T) source;
    }

    /**
     * Returns the invoker used for the invocation.
     */
    public I invoker() {
        return invoker;
    }

    /**
     * Sets the executor for the invocation.
     *
     * @param executor the executor supplier
     * @return the current invocation context
     */
    @SuppressWarnings("unchecked")
    public C executor(Supplier<Result> executor) {
        this.executor = executor;
        return (C) this;
    }

    /**
     * Executes the invocation and returns the result.
     *
     * @return the result of the execution
     */
    public Result execute() {
        if (executor == null) return new Result(source.url(), null);
        return executor.get();
    }

    /**
     * Checks if the context is server-side.
     *
     * @return true if server-side, false otherwise
     */
    public boolean isServerSide() {
        return invoker instanceof Callee<?>;
    }

    /**
     * Checks if the context is client-side.
     *
     * @return true if client-side, false otherwise
     */
    public boolean isClientSide() {
        return invoker instanceof Caller<?>;
    }
}

