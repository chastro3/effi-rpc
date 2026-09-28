package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines fault tolerance options.
 */
public interface FaultToleranceOptions {

    OptionName<String> FAILURE_HANDLER = STRING.currentFirst("faultTolerance.failureHandler");

    OptionName<Integer> RETRIES = INTEGER.currentFirst("faultTolerance.retries", 3);

    OptionName<Integer> RETRY_BACKOFF = INTEGER.currentFirst("faultTolerance.retryBackoff", 100);

    OptionName<Integer> RETRY_MAX_BACKOFF = INTEGER.currentFirst("faultTolerance.retryMaxBackoff", 2000);

    OptionName<Integer> RETRY_JITTER = INTEGER.currentFirst("faultTolerance.retryJitter", 50);
}
