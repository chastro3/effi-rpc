package io.effi.rpc.context.parameter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public record ParameterBinding(Parameter parameter, ParameterResolver<?> resolver) {

    public static ParameterBinding[] emptyResolvers(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            bindings[i] = new ParameterBinding(parameters[i], null);
        }
        return bindings;
    }
}
