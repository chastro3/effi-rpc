package io.effi.rpc.governance.faulttolerance;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.constant.Component.FaultTolerance.FAIL_FAST;

/**
 * Fail-fast implementation of {@link FaultTolerance}.Throw an exception when an operation fails,
 * rather than allowing the operation to continue.
 */
@Extension(FAIL_FAST)
public class FailFast extends AbstractFaultTolerance {

    @Override
    protected void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        throw e;
    }
}

