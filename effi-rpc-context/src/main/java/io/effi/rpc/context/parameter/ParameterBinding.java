package io.effi.rpc.context.parameter;

import java.lang.reflect.Parameter;

/**
 * Binds one method parameter at its method index.
 */
public record ParameterBinding(
        /** Method parameter index. */
        int index,
        /** Reflected method parameter. */
        Parameter parameter,
        /** Caller-side parameter writer. */
        ParameterWriter writer,
        /** Servant-side parameter resolver. */
        ParameterResolver resolver
) {

    public ParameterBinding(int index, Parameter parameter, ParameterBinder binder) {
        this(index, parameter, binder, binder);
    }

    public static ParameterBinding positional(int index, Parameter parameter) {
        return new ParameterBinding(index, parameter, null, null);
    }
}
