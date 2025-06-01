package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.config.transport.EndpointConfigBuilder;

/**
 * Builds http1.1 {@link EndpointConfig} instance and defines configuration.
 */
public interface Http1EndpointConfigBuilder<T extends EndpointConfig, C extends Http1EndpointConfigBuilder<T, C>> extends EndpointConfigBuilder<T, C> {

    /**
     * Set the maximum length of the initial request line.
     */
    default C maxInitialLineLength(int maxInitialLineLength) {
        config().set(DefaultConfigKeys.MAX_INITIAL_LINE_LENGTH, maxInitialLineLength);
        return returnThis();
    }

    /**
     * Set the maximum header size for HTTP requests.
     */
    default C maxHeaderSize(int maxHeaderSize) {
        config().set(DefaultConfigKeys.MAX_HEADER_SIZE, maxHeaderSize);
        return returnThis();
    }

    /**
     * Set the maximum size of a single chunk in chunked transfer encoding.
     */
    default C maxChunkSize(int maxChunkSize) {
        config().set(DefaultConfigKeys.MAX_CHUNK_SIZE, maxChunkSize);
        return returnThis();
    }
}
