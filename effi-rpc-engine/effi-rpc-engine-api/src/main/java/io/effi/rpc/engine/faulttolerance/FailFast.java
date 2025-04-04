package io.effi.rpc.engine.faulttolerance;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.contract.CompletableReplyFuture;
import io.effi.rpc.contract.faulttolerance.AbstractFaultTolerance;
import io.effi.rpc.contract.faulttolerance.FaultTolerance;

import static io.effi.rpc.common.constant.Component.FaultTolerance.FAIL_FAST;

/**
 * Fail-fast implementation of {@link FaultTolerance}.
 * Throw an exception when an operation fails, rather
 * than allowing the operation to continue.
 */
@Extension(FAIL_FAST)
public class FailFast extends AbstractFaultTolerance {

    @Override
    protected void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        throw e;
    }
}

