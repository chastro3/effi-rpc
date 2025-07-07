package io.effi.rpc.governance.faulttolerance;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.metrics.CallerMetrics;

import static io.effi.rpc.constant.Component.FaultTolerance.FAIL_RETRY;

/**
 * Fail-retry implementation of {@link FaultTolerance}.Attempts to retry an operation a specified
 * number of times before ultimately failing.
 */
@Extension(FAIL_RETRY)
public class FailRetry extends AbstractFaultTolerance {

    private static final Logger logger = LoggerFactory.getLogger(FailRetry.class);

    @Override
    public void doOperation(CompletableReplyFuture future, EffiRpcException e) throws EffiRpcException {
        var context = future.context();
        Caller<?> caller = context.callSide();
        Config config = caller.config();
        int retries = Integer.parseInt(config.get(DefaultConfigNames.RETRIES));
        int errorCount = future.errorCount().incrementAndGet();
        // Retry the operation if the error count is less than or equal to the retries
        if (errorCount <= retries) {
            logger.error("Fail to call service: '{}', retrying: {}", e, context.message().url().uri(), errorCount);
            CallerMetrics callerMetrics = caller.get(CallerMetrics.GENERIC_KEY);
            callerMetrics.retryCount().increment();
            caller.callWithFuture(future);
            return;
        }
        throw e;
    }
}

