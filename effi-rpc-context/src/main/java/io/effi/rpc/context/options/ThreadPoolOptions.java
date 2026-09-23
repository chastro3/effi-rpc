package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines thread pool options.
 */
public interface ThreadPoolOptions {

    OptionName<String> THREAD_POOL = STRING.currentFirst("threadPool.name");
}
