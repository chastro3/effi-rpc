package io.effi.rpc.component.serialization.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.LONG;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines serialization options used by peers.
 */
public interface SerializationOptions {

    OptionName<String> SERIALIZER = STRING.currentFirst("serialization.serializer");

    OptionName<Long> SERIALIZATION_THRESHOLD = LONG.currentFirst("serialization.serializationThreshold");

    OptionName<Long> DESERIALIZATION_THRESHOLD = LONG.currentFirst("serialization.deserializationThreshold");
}
