package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.protocol.http.HttpEndpointConfigBuilder;

/**
 * Builds http1.1 {@link EndpointConfig} instance and defines configuration.
 */
public interface Http1EndpointConfigBuilder<T extends EndpointConfig, SELF extends Http1EndpointConfigBuilder<T, SELF>> extends HttpEndpointConfigBuilder<T, SELF> {

    /**
     * Set the maximum length of the initial request line.
     */
    default SELF maxInitialLineLength(int maxInitialLineLength) {
        config().set(ConfigNames.MAX_INITIAL_LINE_LENGTH, maxInitialLineLength);
        return self();
    }

    /**
     * Set the maximum header size for HTTP requests.
     */
    default SELF maxHeaderSize(int maxHeaderSize) {
        config().set(ConfigNames.MAX_HEADER_SIZE, maxHeaderSize);
        return self();
    }

    /**
     * Set the maximum size of a single chunk in chunked transfer encoding.
     */
    default SELF maxChunkSize(int maxChunkSize) {
        config().set(ConfigNames.MAX_CHUNK_SIZE, maxChunkSize);
        return self();
    }
}
