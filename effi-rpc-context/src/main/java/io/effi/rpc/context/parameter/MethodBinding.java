package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Binds every parameter declared by one method.
 */
public record MethodBinding(MethodSignature signature, ParameterBinding[] parameters, boolean positional) {

    public ParameterBinding parameter(int index) {
        return parameters[index];
    }

    public int size() {
        return parameters.length;
    }

    public static MethodBinding of(Method method, ParameterBinding[] parameters) {
        return new MethodBinding(MethodSignature.of(method), parameters, false);
    }

    public static MethodBinding positional(Method method, ParameterBinder binder) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            bindings[i] = new ParameterBinding(i, parameters[i], binder);
        }
        return new MethodBinding(MethodSignature.of(method), bindings, true);
    }
}
