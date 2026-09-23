package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines HTTP/1 endpoint options.
 */
public interface Http1Options {

    OptionName<Integer> MAX_CHUNK_SIZE = INTEGER.onlyCurrent("http.h1.maxChunkSize");

    OptionName<Integer> MAX_INITIAL_LINE_LENGTH = INTEGER.onlyCurrent("http.h1.maxInitialLineLength");

    OptionName<Integer> MAX_HEADER_SIZE = INTEGER.onlyCurrent("http.h1.maxHeaderSize");

}
