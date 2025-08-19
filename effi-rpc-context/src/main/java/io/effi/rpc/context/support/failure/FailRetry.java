package io.effi.rpc.context.support.failure;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.support.UnaryReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import static io.effi.rpc.context.support.failure.FailRetry.NAME;

/**
 * Fail-retry implementation of {@link FaultTolerance}.Attempts to retry an operation a specified
 * number of times before ultimately failing.
 */
@Extension(NAME)
public class FailRetry implements UnaryReplyFuture.FailureHandler {

    public static final String NAME = "failRetry";

    private static final Logger logger = LoggerFactory.getLogger(FailRetry.class);

    @Override
    public void handle(UnaryReplyFuture future, EffiRpcException e) throws EffiRpcException {
        var context = future.context();
        Caller<?> caller = context.peer();
        Config config = caller.config();
        int retries = config.get(ConfigNames.RETRIES);
        int errorCount = future.errorCount();
        // Retry the operation if the error count is less than or equal to the retries
        if (errorCount <= retries) {
            logger.error("Fail to call service: '{}', retrying: {}", e,
                    context.message().url().baseUrl(), errorCount);
            CallerMetrics callerMetrics = caller.get(CallerMetrics.GENERIC_KEY);
            callerMetrics.retryCount().increment();
            // todo 使用 stage 来调用
            // caller.callWithFuture(future);
            return;
        }
        throw e;
    }
}

