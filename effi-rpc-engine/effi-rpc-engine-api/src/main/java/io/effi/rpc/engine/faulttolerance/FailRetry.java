package io.effi.rpc.engine.faulttolerance;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.CompletableReplyFuture;
import io.effi.rpc.contract.faulttolerance.AbstractFaultTolerance;
import io.effi.rpc.contract.faulttolerance.FaultTolerance;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CallerMetrics;

import static io.effi.rpc.common.constant.Component.FaultTolerance.FAIL_RETRY;

/**
 * Fail-retry implementation of {@link FaultTolerance}.
 * Attempts to retry an operation a specified number of
 * times before ultimately failing.
 */
@Extension(FAIL_RETRY)
public class FailRetry extends AbstractFaultTolerance {

    private static final Logger logger = LoggerFactory.getLogger(FailRetry.class);

    @Override
    public void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        var context = future.context();
        Caller<?> caller = context.invoker();
        Config config = caller.config();
        int retries = Integer.parseInt(config.get(DefaultConfigKeys.RETRIES));
        int errorCount = future.errorCount().get();
        // Retry the operation if the error count is less than or equal to the retries
        if (errorCount <= retries) {
            logger.error("An exception occurred in the calling service: {}, Start retry: {}", e, e.getMessage(), errorCount);
            CallerMetrics callerMetrics = caller.get(CallerMetrics.GENERIC_KEY);
            callerMetrics.retryCount().increment();
            caller.callWithFuture(future);
            return;
        }
        throw e;
    }
}

