package io.effi.rpc.component.transport.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines common transport endpoint options.
 */
public interface TransportOptions {

    OptionName<Integer> SEND_BUFFER_SIZE = INTEGER.onlyCurrent("transport.sendBufferSize");

    OptionName<Integer> RECEIVE_BUFFER_SIZE = INTEGER.onlyCurrent("transport.receiveBufferSize");

    OptionName<Integer> IDLE_COUNT_THRESHOLD = INTEGER.onlyCurrent("transport.idleCountThreshold", 6);

    OptionName<Integer> IDLE_TRIGGER_INTERVAL = INTEGER.onlyCurrent("transport.idleTriggerInterval", 5000);
}
