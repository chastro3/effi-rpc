package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;

/**
 * Defines execution units for RPC processing chains.
 *
 * @see Stage
 * @see Interceptor
 */
public interface ExecutionUnit<
        M extends Message,
        S extends CallSide,
        C extends ExchangeContext<M, S>,
        CHAIN extends ExecutionUnitChain> {

    String EXECUTE_METHOD_NAME = "execute";

    /**
     * Executes this unit and uses {@code chain.proceed} to continue the chain.
     *
     * @param context the current exchange context
     * @param chain   the execution chain
     * @return the result of processing
     */
    Result execute(C context, CHAIN chain);

    /**
     * Specifies this unit's type.
     * <p>Defaults to {@code null}. Override to avoid reflection.</p>
     *
     * @return the unit type or {@code null} if unspecified
     */
    default UnitType<M, S> unitType() {
        return null;
    }
}
