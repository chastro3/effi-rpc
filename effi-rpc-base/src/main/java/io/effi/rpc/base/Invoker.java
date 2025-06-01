package io.effi.rpc.base;

import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.component.ModuleSource;
import io.effi.rpc.config.NodeConfigSource;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.TypeToken;

import java.util.Collection;

/**
 * Represents an invocable unit that encapsulates shared behaviors of caller and callee.
 */
public interface Invoker<R> extends Attributes, Identifiable, NodeConfigSource, ModuleSource {

    /**
     * Returns the invocation protocol.
     */
    String protocol();

    /**
     * Returns the associated query path.
     */
    QueryPath queryPath();

    /**
     * Returns the thread pool used by the invoker.
     */
    ThreadPool threadPool();

    /**
     * Invokes the callee/method with the given arguments.
     *
     * @param args the arguments for the invocation
     * @return the result of the invocation
     * @throws EffiRpcException if an error occurs
     */
    R invoke(Object... args) throws EffiRpcException;

    /**
     * Returns the TypeToken representing the return type.
     */
    TypeToken<?> returnType();

    /**
     * Adds filters to the invoker.
     */
    @SuppressWarnings("rawtypes")
    void addFilters(Collection<Filter> filters);

    @Override
    default String id() {
        return InvokerContainer.invokerKey(protocol(), queryPath().path());
    }
}



