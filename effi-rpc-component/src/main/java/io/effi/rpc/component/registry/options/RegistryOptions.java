package io.effi.rpc.component.registry.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines registry options.
 */
public interface RegistryOptions {

    OptionName<Integer> CONNECT_TIMEOUT = INTEGER.onlyCurrent("registry.connectTimeout", 3000);

    OptionName<Integer> RETRIES = INTEGER.onlyCurrent("registry.retries", 3);

    OptionName<Integer> HEARTBEAT_INTERVAL = INTEGER.onlyCurrent("registry.heartbeatInterval", 5000);
}
