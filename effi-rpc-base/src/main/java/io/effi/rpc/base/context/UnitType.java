package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Messages;
import io.effi.rpc.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declares the supported message and call side types for a {@link ExecutionUnit}.
 *
 * @see CallExecutionUnit
 * @see ReplyExecutionUnit
 */
public final class UnitType<M extends Message, S extends CallSide> {

    private static final Map<Pair<Class<?>, Class<?>>, UnitType<?, ?>> CACHE = new ConcurrentHashMap<>();

    private final Class<M> messageType;

    private final Class<S> callSideType;

    private UnitType(Class<M> messageType, Class<S> callSideType) {
        this.messageType = messageType;
        this.callSideType = callSideType;
    }

    /**
     * Extracts the message and call side types from an execution unit.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static UnitType<?, ?> extract(ExecutionUnit eu) {
        String methodName = ExecutionUnit.EXECUTE_METHOD_NAME;
        AssertUtil.notNull(eu, "execution unit");
        if (eu.unitType() != null) return eu.unitType();
        Class<?> parameterClass = null;
        try {
            if (eu instanceof CallExecutionUnit) {
                parameterClass = CallExecutionUnit.class;
            } else if (eu instanceof ReplyExecutionUnit) {
                parameterClass = ReplyExecutionUnit.class;
            } else {
                throw new IllegalArgumentException(Messages.unSupport("execution unit", eu.getClass()));
            }
            Method doFilter = eu.getClass().getMethod(methodName, parameterClass);
            var parameterType = (ParameterizedType) doFilter.getGenericParameterTypes()[0];
            Type[] arguments = parameterType.getActualTypeArguments();
            return of(
                    (Class<? extends Message>) arguments[0],
                    (Class<? extends CallSide>) ((ParameterizedType) arguments[1]).getRawType()
            );
        } catch (NoSuchMethodException ignored) {
            throw new IllegalStateException("Can't find " + methodName + "(" + parameterClass.getName() + ")");
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends Message, S extends CallSide> UnitType<M, S> of(Class<?> messageType, Class<?> callSideType) {
        Pair<Class<?>, Class<?>> key = Pair.of(messageType, callSideType);
        return (UnitType<M, S>) CACHE.computeIfAbsent(key, k -> new UnitType<>((Class<M>) messageType, (Class<S>) callSideType));
    }

    public Class<M> messageType() {
        return messageType;
    }

    public Class<S> callSideType() {
        return callSideType;
    }
}