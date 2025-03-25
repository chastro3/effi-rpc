package io.effi.rpc.contract.faulttolerance;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.contract.CompletableReplyFuture;

/**
 * Abstract implementation of {@link FaultTolerance}.
 */
public abstract class AbstractFaultTolerance implements FaultTolerance {

    @Override
    public void operation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        if (!future.completableFuture().isDone()) {
            doOperation(future, e);
        }
    }

    protected abstract void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException;

}
