package io.effi.rpc.governance.faulttolerance;

import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.exception.EffiRpcException;

/**
 * Provides an abstract implementation of {@link FaultTolerance}.
 */
public abstract class AbstractFaultTolerance implements FaultTolerance {

    @Override
    public void operation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        if (!future.completed()) {
            doOperation(future, e);
        }
    }

    protected abstract void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException;

}
