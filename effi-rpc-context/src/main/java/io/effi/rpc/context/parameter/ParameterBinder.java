package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.Invocation;

/**
 * Binds one method parameter in both directions.
 */
public interface ParameterBinder {

    /**
     * Binds one caller value into the invocation.
     *
     * @param value caller value
     * @param binding parameter binding
     * @param invocation target invocation
     */
    void bind(Object value, ParameterBinding binding, Invocation invocation);

    /**
     * Resolves one servant-side argument from the invocation.
     *
     * @param binding parameter binding
     * @param invocation source invocation
     * @return resolved argument value
     */
    Object resolve(ParameterBinding binding, Invocation invocation);
}
