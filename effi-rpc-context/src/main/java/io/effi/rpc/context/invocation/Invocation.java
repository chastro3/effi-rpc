package io.effi.rpc.context.invocation;

import io.effi.rpc.util.Attributes;

/**
 * Defines one caller-side invocation.
 */
public interface Invocation extends Attributes.Supplier {

    /**
     * Returns the ordered invocation arguments.
     */
    InvocationArguments arguments();

    @Override
    InvocationAttributes attributes();

}
