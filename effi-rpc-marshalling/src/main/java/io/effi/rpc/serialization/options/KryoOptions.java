package io.effi.rpc.serialization.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines Kryo serialization options.
 */
public interface KryoOptions {

    OptionName<String[]> REGISTERED_CLASS_NAMES = STRING_ARRAY.currentFirst("serialization.kryo.registeredClasses");

    OptionName<String[]> INCLUDE_CLASS_NAMES = STRING_ARRAY.mergeParent("serialization.kryo.includeClasses");
}
