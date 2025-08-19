package io.effi.rpc.context.support.failure;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.support.UnaryReplyFuture;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.context.support.failure.FailFast.NAME;

/**
 * Fail-fast implementation of {@link FaultTolerance}.Throw an exception when an operation fails,
 * rather than allowing the operation to continue.
 */
@Extension(NAME)
public class FailFast implements UnaryReplyFuture.FailureHandler {

    public static final String NAME = "failFast";

    @Override
    public void handle(UnaryReplyFuture future, EffiRpcException e) throws EffiRpcException {
        throw e;
    }
}

