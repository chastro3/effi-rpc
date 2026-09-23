package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines caller options.
 */
public interface CallerOptions {

    OptionName<String> ENDPOINT = STRING.currentFirst("call.endpoint");

    OptionName<String> REMOTE_PLATFORM = STRING.currentFirst("call.remotePlatform");

    OptionName<String> REMOTE_APPLICATION = STRING.currentFirst("call.remoteApplication");

    OptionName<String> REMOTE_MODULE = STRING.currentFirst("call.remoteModule");

    OptionName<String> CLIENT = STRING.currentFirst("call.client");

    OptionName<String> PROTOCOL = STRING.currentFirst("call.protocol");

    OptionName<Integer> TIMEOUT = INTEGER.currentFirst("call.timeout", 3000);

    OptionName<String> PROXY = STRING.onlyCurrent("call.proxy");
}
