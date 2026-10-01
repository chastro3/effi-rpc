package io.effi.rpc.component.event;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.BOOLEAN;
import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.LONG;

/**
 * Defines platform-level event bus options.
 */
public interface EventOptions {

    OptionName<Integer> CAPACITY = INTEGER.onlyCurrent("event.capacity", 16_384);

    OptionName<Integer> BATCH_SIZE = INTEGER.onlyCurrent("event.batchSize", 256);

    OptionName<Long> IDLE_PARK_NANOS = LONG.onlyCurrent("event.idleParkNanos", 100_000L);

    OptionName<Long> PUBLISH_TIMEOUT_NANOS = LONG.onlyCurrent("event.publishTimeoutNanos", 100_000_000L);

    OptionName<Boolean> DAEMON = BOOLEAN.onlyCurrent("event.daemon", true);

    OptionName<Integer> TELEMETRY_CONSUMERS = INTEGER.onlyCurrent("event.telemetryConsumers", 1);
}
