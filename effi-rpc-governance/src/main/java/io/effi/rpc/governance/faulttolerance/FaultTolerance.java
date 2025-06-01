package io.effi.rpc.governance.faulttolerance;

import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.annotation.spi.Extensible;

import static io.effi.rpc.constant.Component.FaultTolerance.FAIL_FAST;

/**
 * Handles fault tolerance during RPC calls.
 */
@Extensible(value = FAIL_FAST, key = ExtensionKeys.FAULT_TOLERANCE)
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






