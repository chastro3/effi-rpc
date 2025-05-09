package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.engine.builder.ClientConfigBuilder;

import static io.effi.rpc.constant.Component.Protocol.H2;

/**
 * Builder for HTTP/2 client configuration.
 */
public class Http2ClientConfigBuilder extends ClientConfigBuilder<Http2ClientConfig, Http2ClientConfigBuilder> {

    /**
     * Constructor that sets the default protocol to HTTP/2 and enables SSL.
     */
    public Http2ClientConfigBuilder() {
        this.protocol = H2;
    }

    /**
     * Sets the size of the HPACK header compression table.
     *
     * @param headerTableSize the maximum header table size
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder headerTableSize(int headerTableSize) {
        config.set(DefaultConfigKeys.HEADER_TABLE_SIZE.key(), String.valueOf(headerTableSize));
        return returnThis();
    }

    /**
     * Sets the initial window size for HTTP/2 flow control.
     *
     * @param initialWindowSize the initial window size
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder initialWindowSize(int initialWindowSize) {
        config.set(DefaultConfigKeys.INITIAL_WINDOW_SIZE.key(), String.valueOf(initialWindowSize));
        return returnThis();
    }

    /**
     * Sets the maximum number of concurrent streams allowed per connection.
     *
     * @param maxConcurrentStreams the maximum number of concurrent streams
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder maxConcurrentStreams(int maxConcurrentStreams) {
        config.set(DefaultConfigKeys.MAX_CONCURRENT_STREAMS.key(), String.valueOf(maxConcurrentStreams));
        return returnThis();
    }

    /**
     * Sets the maximum size for HTTP/2 frames.
     *
     * @param maxFrameSize the maximum frame size
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder maxFrameSize(int maxFrameSize) {
        config.set(DefaultConfigKeys.MAX_FRAME_SIZE.key(), String.valueOf(maxFrameSize));
        return returnThis();
    }

    /**
     * Sets the maximum size for the HTTP/2 header list.
     *
     * @param maxHeaderListSize the maximum header list size
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder maxHeaderListSize(int maxHeaderListSize) {
        config.set(DefaultConfigKeys.MAX_HEADER_LIST_SIZE.key(), String.valueOf(maxHeaderListSize));
        return returnThis();
    }

    /**
     * Enables or disables HTTP/2 server push.
     *
     * @param pushEnabled true to enable server push, false to disable
     * @return the current builder instance for chaining
     */
    public Http2ClientConfigBuilder pushEnabled(boolean pushEnabled) {
        config.set(DefaultConfigKeys.PUSH_ENABLED.key(), String.valueOf(pushEnabled));
        return returnThis();
    }

    @Override
    protected Http2ClientConfig build(Config config) {
        return new Http2ClientConfig(protocol, name, config, certificateConfig);
    }
}
