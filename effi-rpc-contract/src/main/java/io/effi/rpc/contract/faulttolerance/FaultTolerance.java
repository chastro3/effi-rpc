package io.effi.rpc.contract.faulttolerance;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.spi.Extensible;
import io.effi.rpc.contract.CompletableReplyFuture;

import static io.effi.rpc.constant.Component.FaultTolerance.FAIL_FAST;

/**
 * Handles fault tolerance during RPC calls.
 */
@Extensible(FAIL_FAST)
public interface FaultTolerance {

    /**
     * Executes fault tolerance logic.
     *
     * @param future the associated future
     * @param e      the exception causing the failure
     * @throws EffiRpcException if an exception is thrown, the fault tolerance process is complete
     */
    void operation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException;
}






