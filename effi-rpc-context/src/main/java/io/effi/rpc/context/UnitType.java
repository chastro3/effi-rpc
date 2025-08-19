package io.effi.rpc.context;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declares the supported message and call side types for execution units.
 * <p>
 * Provides type information for execution units including message and call side
 * type parameters with caching support for efficient type extraction.
 * </p>
 *
 * @see CallExecutionUnit
 * @see ReplyExecutionUnit
 */
public final class UnitType<M extends Message, P extends Peer> {

    private static final Map<Pair<Class<?>, Class<?>>, UnitType<?, ?>> CACHE = new ConcurrentHashMap<>();

    private final Class<M> messageType;

    private final Class<P> peerType;

    private UnitType(Class<M> messageType, Class<P> peerType) {
        this.messageType = messageType;
        this.peerType = peerType;
    }

    /**
     * Extracts the message and call side types from an execution unit.
     * Attempts to retrieve from the unit's declared type first, then uses reflection.
     *
     * @param unit the execution unit to extract types from
     * @return the extracted unit type
     * @throws IllegalStateException if unable to determine the unit type
     */
    @SuppressWarnings({"rawtypes"})
    public static UnitType<?, ?> extract(Interaction.Unit unit) {
        AssertUtil.notNull(unit, "execution unit");
        if (unit.unitType() != null) return unit.unitType();
        Class<?> clazz = unit.getClass();
        while (clazz != null) {
            for (Type interfaceType : clazz.getGenericInterfaces()) {
                if (interfaceType instanceof ParameterizedType pt &&
                        pt.getRawType() instanceof Class<?> raw &&
                        Interaction.Unit.class.isAssignableFrom(raw)) {
                    Type mType = pt.getActualTypeArguments()[0];
                    Type pType = pt.getActualTypeArguments()[1];
                    return of((Class) mType, (Class) pType);
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Cannot determine UnitType for: " + unit.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <M extends Message, P extends Peer> UnitType<M, P> of(Class<?> messageType, Class<?> peerType) {
        Pair<Class<?>, Class<?>> key = Pair.of(messageType, peerType);
        return (UnitType<M, P>) CACHE.computeIfAbsent(key, k -> new UnitType<>((Class<M>) messageType, (Class<P>) peerType));
    }

    public Class<M> messageType() {
        return messageType;
    }

    public Class<P> peerType() {
        return peerType;
    }
}