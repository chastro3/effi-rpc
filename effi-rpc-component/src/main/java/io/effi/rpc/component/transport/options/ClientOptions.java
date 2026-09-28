package io.effi.rpc.component.transport.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines client transport options.
 */
public interface ClientOptions {

    OptionName<Integer> MAX_CONNECTIONS = INTEGER.onlyCurrent("transport.client.maxConnections", 3);

    OptionName<Integer> MAX_PENDING_ACQUIRES = INTEGER.onlyCurrent("transport.client.maxPendingAcquires", 1024);

    OptionName<Integer> ACQUIRE_TIMEOUT = INTEGER.onlyCurrent("transport.client.acquireTimeout", 3000);

    OptionName<Integer> CONNECT_TIMEOUT = INTEGER.onlyCurrent("transport.client.connectTimeout", 3000);
}
