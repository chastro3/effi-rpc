package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.option.OptionName;
import io.netty.handler.codec.http.HttpObjectDecoder;

import static io.effi.rpc.option.OptionTypes.INTEGER;

/**
 * Defines HTTP/1 endpoint options.
 */
public interface Http1Options {

    OptionName<Integer> MAX_CHUNK_SIZE =
            INTEGER.onlyCurrent("http.h1.maxChunkSize", HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE);

    OptionName<Integer> MAX_INITIAL_LINE_LENGTH =
            INTEGER.onlyCurrent("http.h1.maxInitialLineLength", HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH);

    OptionName<Integer> MAX_HEADER_SIZE =
            INTEGER.onlyCurrent("http.h1.maxHeaderSize", HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE);

}
