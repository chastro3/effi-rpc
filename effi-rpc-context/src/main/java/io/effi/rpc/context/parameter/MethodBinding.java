package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Binds every parameter declared by one method.
 */
public record MethodBinding(
        /** Method signature. */
        MethodSignature signature,
        /** Parameter bindings in declaration order. */
        ParameterBinding[] parameters,
        /** Whether bindings use positional mapping. */
        boolean positional
) {

    /**
     * Creates a non-positional method binding.
     *
     * @param method reflected method
     * @param parameters parameter bindings
     * @return method binding
     */
    public static MethodBinding of(Method method, ParameterBinding[] parameters) {
        return new MethodBinding(MethodSignature.of(method), parameters, false);
    }

    /**
     * Creates a positional method binding.
     *
     * @param method reflected method
     * @param binder parameter binder
     * @return positional method binding
     */
    public static MethodBinding positional(Method method, ParameterBinder binder) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            bindings[i] = new ParameterBinding(i, parameters[i], binder);
        }
        return new MethodBinding(MethodSignature.of(method), bindings, true);
    }

    /**
     * Returns the parameter binding at the supplied index.
     *
     * @param index parameter index
     * @return parameter binding
     */
    public ParameterBinding parameter(int index) {
        return parameters[index];
    }

    /**
     * Returns the parameter count.
     */
    public int size() {
        return parameters.length;
    }
}
