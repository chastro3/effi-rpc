package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.INTEGER;
import static io.effi.rpc.option.OptionTypes.LONG;

/**
 * Defines HTTP/2 endpoint options.
 */
public interface Http2Options {

    OptionName<Long> HEADER_TABLE_SIZE = LONG.onlyCurrent("http.h2.headerTableSize", 4096L);

    OptionName<Long> MAX_CONCURRENT_STREAMS = LONG.onlyCurrent("http.h2.maxConcurrentStreams", 1000L);

    OptionName<Integer> INITIAL_WINDOW_SIZE = INTEGER.onlyCurrent("http.h2.initialWindowSize", 65535 * 20);

    OptionName<Integer> MAX_FRAME_SIZE = INTEGER.onlyCurrent("http.h2.maxFrameSize", 16384);

    OptionName<Integer> MAX_HEADER_LIST_SIZE = INTEGER.onlyCurrent("http.h2.maxHeaderListSize", 8192);

}
