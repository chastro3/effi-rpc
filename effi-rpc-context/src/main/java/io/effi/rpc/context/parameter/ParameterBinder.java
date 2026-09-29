package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.Invocation;

/**
 * Binds one method parameter in both directions.
 */
public interface ParameterBinder {

    void bind(Object value, ParameterBinding binding, Invocation invocation);

    Object resolve(ParameterBinding binding, Invocation invocation);
}
