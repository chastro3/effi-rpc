package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines peer configurator options.
 */
public interface ConfiguratorOptions {

    OptionName<String> THREAD_POOL_CONFIGURATOR = STRING.currentFirst("configurator.threadPool");

    OptionName<String> STAGE_CHAIN_CONFIGURATOR = STRING.currentFirst("configurator.stageChain");

    OptionName<String> INTERCEPTOR_CHAIN_CONFIGURATOR = STRING.currentFirst("configurator.interceptorChain");

}
