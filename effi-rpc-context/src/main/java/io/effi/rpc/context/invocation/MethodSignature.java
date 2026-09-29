package io.effi.rpc.context.invocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Identifies a method by name and parameter types.
 */
public record MethodSignature(String name, Class<?>[] parameterTypes) {

    public static MethodSignature of(Method method) {
        return new MethodSignature(method.getName(), method.getParameterTypes());
    }

    @Override
    public String toString() {
        String parameters = Arrays.stream(parameterTypes)
                .map(Class::getName)
                .collect(Collectors.joining(","));
        return name + "(" + parameters + ")";
    }
}
