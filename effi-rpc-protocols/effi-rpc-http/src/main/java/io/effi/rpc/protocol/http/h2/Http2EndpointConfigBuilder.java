package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.protocol.http.HttpEndpointConfigBuilder;

/**
 * Builds http2 {@link EndpointConfig} instance and defines configuration.
 */
public interface Http2EndpointConfigBuilder<T extends EndpointConfig, SELF extends Http2EndpointConfigBuilder<T, SELF>> extends HttpEndpointConfigBuilder<T, SELF> {

    /**
     * Sets the size of the HPACK header compression table.
     */
    default SELF headerTableSize(long headerTableSize) {
        config().set(ConfigNames.HEADER_TABLE_SIZE, headerTableSize);
        return self();
    }

    /**
     * Sets the initial window size for HTTP/2 flow control.
     */
    default SELF initialWindowSize(int initialWindowSize) {
        config().set(ConfigNames.INITIAL_WINDOW_SIZE, initialWindowSize);
        return self();
    }

    /**
     * Sets the maximum number of concurrent streams allowed per connection.
     */
    default SELF maxConcurrentStreams(long maxConcurrentStreams) {
        config().set(ConfigNames.MAX_CONCURRENT_STREAMS, maxConcurrentStreams);
        return self();
    }

    /**
     * Sets the maximum size for HTTP/2 frames.
     */
    default SELF maxFrameSize(int maxFrameSize) {
        config().set(ConfigNames.MAX_FRAME_SIZE, maxFrameSize);
        return self();
    }

    /**
     * Sets the maximum size for the HTTP/2 header list.
     */
    default SELF maxHeaderListSize(int maxHeaderListSize) {
        config().set(ConfigNames.MAX_HEADER_LIST_SIZE, maxHeaderListSize);
        return self();
    }
}
