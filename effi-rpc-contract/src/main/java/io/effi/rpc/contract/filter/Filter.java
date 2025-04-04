package io.effi.rpc.contract.filter;

import io.effi.rpc.common.util.Ordered;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.ExecutorContext;

/**
 * Base interface for filters that intercept RPC calls.
 * <p>
 * Client flow:
 * InvokerFilter -> Locator -> ChosenFilter -> Send Request -> Received Response -> ReplyFilter
 * </p>
 * <p>
 * Server flow:
 * InvokerFilter -> Invoke Callee -> ReplyFilter
 * </p>
 *
 * @param <T> the type of the message
 * @param <I> the type of the invoker
 * @param <C> the type of the execution context
 * @see InvokeFilter
 * @see ChosenFilter
 * @see ReplyFilter
 */
@FunctionalInterface
public interface Filter<T extends Envelope, I extends Invoker<?>, C extends ExecutorContext<T, I, ?>> extends Ordered {

    /**
     * Intercepts and processes an RPC call.
     * Typically, {@link ExecutorContext#execute} is used to return the result.
     *
     * @param context the execution context
     * @return the result of the filter process
     */
    Result doFilter(C context);

    default FilterType<T, I> type() {
        return null;
    }


}



