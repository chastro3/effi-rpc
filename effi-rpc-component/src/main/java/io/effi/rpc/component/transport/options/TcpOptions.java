package io.effi.rpc.component.transport.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.BOOLEAN;

/**
 * Defines TCP transport options.
 */
public interface TcpOptions {

    OptionName<Boolean> NO_DELAY = BOOLEAN.onlyCurrent("transport.tcp.noDelay", true);

    OptionName<Boolean> KEEP_ALIVE = BOOLEAN.onlyCurrent("transport.tcp.keepAlive", true);

    OptionName<Boolean> SSL = BOOLEAN.onlyCurrent("transport.tcp.ssl", false);
}
