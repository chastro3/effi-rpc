package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines interceptor options.
 */
public interface InterceptorOptions {

    OptionName<String[]> INCLUDE = STRING_ARRAY.mergeParent("interceptor.include");

    OptionName<String[]> EXCLUDE = STRING_ARRAY.onlyCurrent("interceptor.exclude");
}
