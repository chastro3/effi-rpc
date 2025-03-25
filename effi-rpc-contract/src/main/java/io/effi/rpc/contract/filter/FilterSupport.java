package io.effi.rpc.contract.filter;

import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Handle filter-related operations.
 */
public class FilterSupport {

    /**
     * Checks if a filter is supported.
     *
     * @param filter the filter to check
     * @return {@code true} if the filter is supported, {@code false} otherwise
     */
    public static boolean isSupport(Filter<?, ?, ?> filter) {
        if (filter == null) return false;
        try {
            getType(filter);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the types of the envelope and invoker from a filter.
     *
     * @param filter the filter to inspect
     * @return a {@link Type} object containing the envelope and invoker types
     * @throws IllegalStateException if the {@code doFilter} method cannot be found
     * @throws NullPointerException  if the filter is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static Type getType(Filter<?, ?, ?> filter) {
        Class<?> parameterClass = null;
        try {
            AssertUtil.notNull(filter, "filter");
            if (filter instanceof InvokeFilter<?, ?> || filter instanceof ChosenFilter<?, ?>) {
                parameterClass = InvocationContext.class;
            } else if (filter instanceof ReplyFilter<?, ?>) {
                parameterClass = ReplyContext.class;
            } else {
                throw new IllegalArgumentException(Messages.unSupport("filter", filter.getClass()));
            }
            Method doFilter = filter.getClass().getMethod("doFilter", parameterClass);
            var parameterType = (ParameterizedType) doFilter.getGenericParameterTypes()[0];
            java.lang.reflect.Type[] arguments = parameterType.getActualTypeArguments();
            return new Type((Class<? extends Envelope>) arguments[0],
                    (Class<? extends Invoker<?>>) ((ParameterizedType) arguments[1]).getRawType());
        } catch (NoSuchMethodException ignored) {
            throw new IllegalStateException("Can't find doFilter(" + parameterClass.getName() + ")");
        }
    }

    /**
     * Hold the envelope and invoker types.
     *
     * @param envelopeType the class type of the envelope
     * @param invokerType  the class type of the invoker
     */
    public record Type(Class<? extends Envelope> envelopeType, Class<? extends Invoker<?>> invokerType) {

    }
}
