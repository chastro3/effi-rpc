package io.effi.rpc.base.context;

import io.effi.rpc.base.Result;

/**
 * Defines execution chains for execution units.
 */
@SuppressWarnings("rawtypes")
public interface ExecutionUnitChain {

    /**
     * Proceeds from the start of the chain or invokes the next unit.
     *
     * @param context the current exchange context
     * @return the result of processing
     */
    <C extends ExchangeContext> Result proceed(C context);

}
