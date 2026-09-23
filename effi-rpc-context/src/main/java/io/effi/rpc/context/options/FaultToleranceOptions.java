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
}
