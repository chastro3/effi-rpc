package io.effi.rpc.base.filter;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.context.ExecutorContext;
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
@ScopedComponent
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




