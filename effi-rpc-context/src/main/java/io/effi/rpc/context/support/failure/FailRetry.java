package io.effi.rpc.context.support.failure;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.support.Unary;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.context.support.failure.FailRetry.NAME;

/**
 * Fail-retry implementation of {@link FaultTolerance}.Attempts to retry an operation a specified
 * number of times before ultimately failing.
 */
@Extension(NAME)
public class FailRetry implements Unary.FailureHandler {

    public static final String NAME = "failRetry";

    public static final OptionName<Integer> RETRIES = ConfigurableOptionName.<Integer>nameOf("retries", CURRENT_FIRST).defaultValue(3);

    private static final Logger logger = LoggerFactory.getLogger(FailRetry.class);

    @Override
    public void handle(Unary.ReplyFuture future, EffiRpcException e) throws EffiRpcException {
        // todo 交给scheduler来执行
        var context = future.context();
        Caller<?> caller = context.peer();
        int retries = caller.option(RETRIES);
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

