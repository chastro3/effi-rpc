package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.util.Ordered;

/**
 * Intercepts RPC calls during invocation or reply phases.
 * <p>
 * Client flow: InvokerFilter → Locator → ChosenFilter → Send Request → Receive Response → ReplyFilter.
 * Server flow: InvokerFilter → Invoke Callee → ReplyFilter.
 * </p>
 *
 * @see InvokeFilter
 * @see ChosenFilter
 * @see ReplyFilter
 */
@FunctionalInterface
public interface Filter<T extends Envelope, I extends Invoker<?>, C extends ExecutorContext<T, I, ?>> extends Ordered {

    /**
     * Processes the RPC call.
     * Typically, uses {@link ExecutorContext#execute()} to return the result.
     *
     * @param context the execution context
     * @return the result of filtering
     */
    Result doFilter(C context);

    /**
     * Checks if a filter is supported.
     */
    static boolean isSupported(Filter<?, ?, ?> filter) {
        if (filter == null) return false;
        try {
            FilterType.extract(filter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the filter type.
     * <p>
     * Defaults to {@code null}. Frameworks can infer the {@link FilterType} using reflection,
     * but overriding this avoids reflection overhead.
     * </p>
     */
    default FilterType<T, I> type() {
        return null;
    }
}




