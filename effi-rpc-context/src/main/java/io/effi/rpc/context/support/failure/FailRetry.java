package io.effi.rpc.context.support.failure;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.context.support.Unary;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import static io.effi.rpc.context.support.failure.FailRetry.NAME;

/**
 * Fail-retry implementation of {@link FaultTolerance}.Attempts to retry an operation a specified
 * number of times before ultimately failing.
 */
@Extension(NAME)
public class FailRetry implements Unary.FailureHandler {

    public static final String NAME = "failRetry";

    private static final Logger logger = LoggerFactory.getLogger(FailRetry.class);

    @Override
    public void handle(CallContext<Request, Caller<?>> context, int failureCount, EffiRpcException cause) throws EffiRpcException {
        Caller<?> caller = context.peer();
        if (!retryable(cause)) {
            throw cause;
        }
        int retries = caller.option(FaultToleranceOptions.RETRIES);
        if (failureCount <= retries) {
            logger.error("Fail to call service: '{}', retrying: {}", cause, context.message().url().baseUrl(), failureCount);
            CallerMetrics callerMetrics = caller.get(CallerMetrics.GENERIC_KEY);
            callerMetrics.retryCount().increment();
            return;
        }
        throw cause;
    }

    private static boolean retryable(EffiRpcException cause) {
        return Boolean.parseBoolean(cause.metadata().get(KeyConstant.RETRYABLE))
                || cause.errorCode() == PredefinedErrorCode.SERVICE_UNAVAILABLE
                || cause.errorCode() == InteractionErrorCodes.SERVER_OVERLOADED;
    }
}

