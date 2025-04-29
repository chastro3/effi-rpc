package io.effi.rpc.contract;

import io.effi.rpc.config.NodeConfigSource;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.repository.ComponentRepository;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.TypeToken;

/**
 * Wraps client and server invocations.
 * Clients invoke methods via {@link #invoke(Object...)}, servers process service methods.
 *
 * @param <R> the return type of the invocation
 */
public interface Invoker<R> extends Attributes, ComponentRepository.Key, NodeConfigSource {

    /**
     * Returns the invocation protocol.
     */
    String protocol();

    /**
     * Returns the associated query path.
     */
    QueryPath queryPath();

    /**
     * Invokes the method with the given arguments.
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
    void addFilter(Filter<?, ?, ?>... filters);

    @Override
    default String repositoryKey() {
        return InvokerContainer.generateInvokerKey(protocol(), queryPath().path());
    }
}



