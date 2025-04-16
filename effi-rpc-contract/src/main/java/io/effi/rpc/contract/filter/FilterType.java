package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a combination of {@link Envelope} and {@link Invoker} types
 * to determine which {@link Filter} is supported.
 *
 * @param <T> the type of the {@link Envelope}
 * @param <I> the type of the {@link Invoker}
 */
public class FilterType<T extends Envelope, I extends Invoker<?>> {

    private static final Map<String, FilterType<?, ?>> CACHE = new ConcurrentHashMap<>();

    private final Class<T> envelopeType;

    private final Class<I> invokerType;

    @SuppressWarnings("unchecked")
    public static <T extends Envelope, I extends Invoker<?>> FilterType<T, I> of(Class<?> envelopeType, Class<?> invokerType) {
        String key = envelopeType.getName() + ":" + invokerType.getName();
        return (FilterType<T, I>) CACHE.computeIfAbsent(key, k -> new FilterType<>((Class<T>) envelopeType, (Class<I>) invokerType));
    }

    FilterType(Class<T> envelopeType, Class<I> invokerType) {
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