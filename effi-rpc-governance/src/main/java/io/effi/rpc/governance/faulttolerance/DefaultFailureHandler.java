package io.effi.rpc.governance.faulttolerance;

import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.spi.ExtensionLoader;

/**
 * Provides a default implementation of {@link CompletableReplyFuture.FailureHandler}.
 */
public final class DefaultFailureHandler implements CompletableReplyFuture.FailureHandler {

    private static final DefaultFailureHandler INSTANCE = new DefaultFailureHandler();

    public static DefaultFailureHandler getInstance() {
        return INSTANCE;
    }

    private DefaultFailureHandler() {

    }

    @Override
    public void handle(CompletableReplyFuture future, EffiRpcException e) {
        String name = future.context().invoker().get(DefaultConfigKeys.FAULT_TOLERANCE);
        FaultTolerance faultTolerance = ExtensionLoader.loadExtension(FaultTolerance.class, name);
        try {
            faultTolerance.operation(future, e);
        } catch (EffiRpcException finalE) {
            future.completableFuture().completeExceptionally(finalE);
        }
    }
}
