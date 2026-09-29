package io.effi.rpc.context.parameter;

import java.lang.reflect.Parameter;

/**
 * Binds one method parameter at its method index.
 */
public record ParameterBinding(int index, Parameter parameter, ParameterBinder binder) {
}
