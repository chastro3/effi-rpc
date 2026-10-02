package io.effi.rpc.context;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Pair;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declares the supported message and peer types of an execution unit.
 * <p>
 * Extracted types are cached per message/peer pair so reflection only runs once.
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
        if (unit.unitType() != null) {
            return unit.unitType();
        }
        Class<?> clazz = unit.getClass();
        while (clazz != null) {
            for (Type interfaceType : clazz.getGenericInterfaces()) {
                if (interfaceType instanceof ParameterizedType pt &&
                        pt.getRawType() instanceof Class<?> raw &&
                        Interaction.Unit.class.isAssignableFrom(raw)) {
                    Type mType = pt.getActualTypeArguments()[0];
                    Type pType = pt.getActualTypeArguments()[1];
                    return cached((Class) mType, (Class) pType);
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Cannot determine UnitType for: " + unit.getClass());
    }

    /**
     * Returns the cached type pair for the supplied message and peer types.
     *
     * @param messageType message type
     * @param peerType    peer type
     * @param <M>         message type parameter
     * @param <P>         peer type parameter
     * @return cached unit type
     */
    @SuppressWarnings("unchecked")
    public static <M extends Message, P extends Peer> UnitType<M, P> cached(Class<?> messageType, Class<?> peerType) {
        Pair<Class<?>, Class<?>> key = Pair.of(messageType, peerType);
        return (UnitType<M, P>) CACHE.computeIfAbsent(key, k -> new UnitType<>((Class<M>) messageType, (Class<P>) peerType));
    }

    /**
     * Returns the message type of this unit.
     */
    public Class<M> messageType() {
        return messageType;
    }

    /**
     * Returns the peer type of this unit.
     */
    public Class<P> peerType() {
        return peerType;
    }
}
