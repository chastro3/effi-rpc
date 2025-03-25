package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.support.builder.ServerConfigBuilder;

import static io.effi.rpc.common.constant.Component.Protocol.H2;

/**
 * Builder for HTTP/2 server configuration.
 */
public class Http2ServerConfigBuilder extends ServerConfigBuilder<Http2ServerConfig, Http2ServerConfigBuilder> {

    public Http2ServerConfigBuilder() {
        this.protocol = H2;
        ssl(true);
    }

    /**
     * Sets the size of the HPACK header compression table.
     *
     * @param headerTableSize the maximum header table size
     * @return the current builder instance for chaining
     */
    public Http2ServerConfigBuilder headerTableSize(int headerTableSize) {
        config.set(DefaultConfigKeys.HEADER_TABLE_SIZE.key(), String.valueOf(headerTableSize));
        return returnThis();
    }

    /**
     * Sets the initial window size for HTTP/2 flow control.
     *
     * @param initialWindowSize the initial window size
     * @return the current builder instance for chaining
     */
    public Http2ServerConfigBuilder initialWindowSize(int initialWindowSize) {
        config.set(DefaultConfigKeys.INITIAL_WINDOW_SIZE.key(), String.valueOf(initialWindowSize));
        return returnThis();
    }

    /**
     * Sets the maximum number of concurrent streams allowed per connection.
     *
     * @param maxConcurrentStreams the maximum number of concurrent streams
     * @return the current builder instance for chaining
     */
    public Http2ServerConfigBuilder maxConcurrentStreams(int maxConcurrentStreams) {
        config.set(DefaultConfigKeys.MAX_CONCURRENT_STREAMS.key(), String.valueOf(maxConcurrentStreams));
        return returnThis();
    }

    /**
     * Sets the maximum size for HTTP/2 frames.
     *
     * @param maxFrameSize the maximum frame size
     * @return the current builder instance for chaining
     */
    public Http2ServerConfigBuilder maxFrameSize(int maxFrameSize) {
        config.set(DefaultConfigKeys.MAX_FRAME_SIZE.key(), String.valueOf(maxFrameSize));
        return returnThis();
    }

    /**
     * Sets the maximum size for the HTTP/2 header list.
     *
     * @param maxHeaderListSize the maximum header list size
     * @return the current builder instance for chaining
     */
    public Http2ServerConfigBuilder maxHeaderListSize(int maxHeaderListSize) {
        config.set(DefaultConfigKeys.MAX_HEADER_LIST_SIZE.key(), String.valueOf(maxHeaderListSize));
        return returnThis();
    }

    @Override
    protected Http2ServerConfig build(Config config) {
        return new Http2ServerConfig(protocol, name, config);
    }
}
