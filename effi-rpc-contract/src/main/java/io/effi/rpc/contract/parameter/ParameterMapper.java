package io.effi.rpc.contract.parameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Holds a method parameter and its corresponding handled value.
 *
 * @param parameter the method parameter
 * @param value     the handled value
 * @param <T>       the type of the handled value
 */
public record ParameterMapper<T>(Parameter parameter, T value) {

    @SuppressWarnings("unchecked")
    public static <T> ParameterMapper<T>[] emptyParsers(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterMapper<T>[] parameterMappers = new ParameterMapper[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterMappers[i] = new ParameterMapper<>(parameters[i], null);
        }
        return parameterMappers;
    }
}
