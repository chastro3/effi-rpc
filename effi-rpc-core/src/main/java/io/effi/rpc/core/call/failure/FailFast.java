package io.effi.rpc.core.call.failure;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.core.call.Unary;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.core.call.failure.FailFast.NAME;

/**
 * Provides fail-fast failure handling for unary RPC calls.
 */
@Extension(value = NAME, primary = true)
public class FailFast implements Unary.FailureHandler {

    public static final String NAME = "failFast";

    @Override
    public void handle(CallContext<Request, Caller<?>> context, int failureCount, EffiRpcException cause) throws EffiRpcException {
        throw cause;
    }
}

