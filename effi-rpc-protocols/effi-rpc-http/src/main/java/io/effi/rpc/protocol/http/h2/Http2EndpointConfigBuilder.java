package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.config.transport.EndpointConfigBuilder;

/**
 * Builds http2 {@link EndpointConfig} instance and defines configuration.
 */
public interface Http2EndpointConfigBuilder<T extends EndpointConfig, C extends Http2EndpointConfigBuilder<T, C>> extends EndpointConfigBuilder<T, C> {

    /**
     * Sets the size of the HPACK header compression table.
     */
    default C headerTableSize(int headerTableSize) {
        config().set(DefaultConfigNames.HEADER_TABLE_SIZE, headerTableSize);
        return returnThis();
    }

    /**
     * Sets the initial window size for HTTP/2 flow control.
     */
    default C initialWindowSize(int initialWindowSize) {
        config().set(DefaultConfigNames.INITIAL_WINDOW_SIZE, initialWindowSize);
        return returnThis();
    }

    /**
     * Sets the maximum number of concurrent streams allowed per connection.
     */
    default C maxConcurrentStreams(int maxConcurrentStreams) {
        config().set(DefaultConfigNames.MAX_CONCURRENT_STREAMS, maxConcurrentStreams);
        return returnThis();
    }

    /**
     * Sets the maximum size for HTTP/2 frames.
     */
    default C maxFrameSize(int maxFrameSize) {
        config().set(DefaultConfigNames.MAX_FRAME_SIZE, maxFrameSize);
        return returnThis();
    }

    /**
     * Sets the maximum size for the HTTP/2 header list.
     */
    default C maxHeaderListSize(int maxHeaderListSize) {
        config().set(DefaultConfigNames.MAX_HEADER_LIST_SIZE, maxHeaderListSize);
        return returnThis();
    }
}
