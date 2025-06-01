package io.effi.rpc.base.filter;

import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declares the supported envelope and invoker types for a {@link Filter}.
 */
public final class FilterType<T extends Envelope, I extends Invoker<?>> {

    private static final Map<Pair<Class<?>,Class<?>>, FilterType<?, ?>> CACHE = new ConcurrentHashMap<>();

    private final Class<T> envelopeType;

    private final Class<I> invokerType;

    @SuppressWarnings("unchecked")
    public static <T extends Envelope, I extends Invoker<?>> FilterType<T, I> of(Class<?> envelopeType, Class<?> invokerType) {
        Pair<Class<?>, Class<?>> key = Pair.of(envelopeType, invokerType);
        return (FilterType<T, I>) CACHE.computeIfAbsent(key, k -> new FilterType<>((Class<T>) envelopeType, (Class<I>) invokerType));
    }

    /**
     * Extracts the envelope and invoker types from a filter.
     */
    @SuppressWarnings("unchecked")
    public static FilterType<?, ?> extract(Filter<?, ?, ?> filter) {
        AssertUtil.notNull(filter, "filter");
        if (filter.type() != null) return filter.type();
        Class<?> parameterClass = null;
        try {
            if (filter instanceof InvokeFilter<?, ?> || filter instanceof ChosenFilter<?, ?>) {
                parameterClass = InvocationContext.class;
            } else if (filter instanceof ReplyFilter<?, ?>) {
                parameterClass = ReplyContext.class;
            } else {
                throw new IllegalArgumentException(Messages.unSupport("filter", filter.getClass()));
            }
            Method doFilter = filter.getClass().getMethod("doFilter", parameterClass);
            var parameterType = (ParameterizedType) doFilter.getGenericParameterTypes()[0];
            Type[] arguments = parameterType.getActualTypeArguments();
            return of(
                    (Class<? extends Envelope>) arguments[0],
                    (Class<? extends Invoker<?>>) ((ParameterizedType) arguments[1]).getRawType()
            );
        } catch (NoSuchMethodException ignored) {
            throw new IllegalStateException("Can't find doFilter(" + parameterClass.getName() + ")");
        }
    }

    private FilterType(Class<T> envelopeType, Class<I> invokerType) {
        this.envelopeType = envelopeType;
        this.invokerType = invokerType;
    }

    public Class<T> envelopeType() {
        return envelopeType;
    }

    public Class<I> invokerType() {
        return invokerType;
    }
}