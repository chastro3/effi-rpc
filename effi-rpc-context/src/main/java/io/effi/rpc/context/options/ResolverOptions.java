package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines peer resolver options.
 */
public interface ResolverOptions {

    OptionName<String> THREAD_POOL_RESOLVER = STRING.currentFirst("resolver.threadPool");

    OptionName<String> STAGE_CHAIN_RESOLVER = STRING.currentFirst("resolver.stageChain");

    OptionName<String> INTERCEPTOR_CHAIN_RESOLVER = STRING.currentFirst("resolver.interceptorChain");

}
