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
        /** Parameter binder. */
        ParameterBinder binder
) {
}
