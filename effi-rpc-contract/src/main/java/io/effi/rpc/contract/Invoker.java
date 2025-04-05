package io.effi.rpc.contract;

import io.effi.rpc.common.config.LinkedConfigSource;
import io.effi.rpc.common.config.QueryPath;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.util.Attributes;
import io.effi.rpc.common.util.GenerateUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.manager.ComponentManager;

/**
 * Wrapper for client and server invocation.
 * <p>
 * Provides a unified mechanism for invoking methods on both client and server sides.
 * Clients use {@link #invoke(Object...)} to initiate remote calls, while servers
 * invoke corresponding service methods for seamless interaction.
 *
 * @param <R> the return type of the invocation
 */
public interface Invoker<R> extends Attributes, ComponentManager.Key, LinkedConfigSource {

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
     *
     * @param filters filters to process requests and responses
     */
    void addFilter(Filter<?, ?, ?>... filters);

    @Override
    default String managerKey() {
        return GenerateUtil.generateInvokerKey(protocol(), queryPath().path());
    }
}



