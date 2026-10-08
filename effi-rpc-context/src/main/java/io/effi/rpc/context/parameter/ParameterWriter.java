package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.Invocation;

/**
 * Writes caller-side method arguments into an invocation.
 */
@FunctionalInterface
public interface ParameterWriter {

    /**
     * Binds one caller value into the invocation.
     *
     * @param value      caller value
     * @param binding    parameter binding
     * @param invocation target invocation
     */
    void write(Object value, ParameterBinding binding, Invocation invocation);
}
