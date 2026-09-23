package io.effi.rpc.component.serialization.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.STRING;

/**
 * Defines compression options.
 */
public interface CompressionOptions {

    OptionName<String> COMPRESSOR = STRING.currentFirst("compression.compressor");

    OptionName<Integer> MAX_DECOMPRESSED_BYTES = INTEGER.onlyCurrent("compression.maxDecompressedBytes", 16 * 1024 * 1024);
}
