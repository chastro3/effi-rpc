package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;
import io.effi.rpc.protocol.http.HttpEndpointConfig;

public abstract class Http2EndpointConfig extends HttpEndpointConfig {

    public static final OptionName<Long> HEADER_TABLE_SIZE = ConfigurableOptionName.<Long>nameOf("headerTableSize").defaultValue(4096L);

    public static final OptionName<Long> MAX_CONCURRENT_STREAMS = ConfigurableOptionName.<Long>nameOf("maxConcurrentStreams").defaultValue(1000L);

    public static final OptionName<Integer> INITIAL_WINDOW_SIZE = ConfigurableOptionName.<Integer>nameOf("initialWindowSize").defaultValue(65535 * 20);

    public static final OptionName<Integer> MAX_FRAME_SIZE = ConfigurableOptionName.<Integer>nameOf("maxFrameSize").defaultValue(16384);

    public static final OptionName<Integer> MAX_HEADER_LIST_SIZE = ConfigurableOptionName.<Integer>nameOf("maxHeaderListSize").defaultValue(8192);

    protected Http2EndpointConfig(String id, Options options) {
        super(Http2Protocol.VERSION, id, options);
    }

    public interface Configurator<SELF extends Configurator<SELF>> extends HttpEndpointConfig.Configurator<SELF> {

        /**
         * Sets the size of the HPACK header compression table.
         */
        default SELF headerTableSize(long headerTableSize) {
            addOption(HEADER_TABLE_SIZE, headerTableSize);
            return self();
        }

        /**
         * Sets the initial window size for HTTP/2 flow control.
         */
        default SELF initialWindowSize(int initialWindowSize) {
            addOption(INITIAL_WINDOW_SIZE, initialWindowSize);
            return self();
        }

        /**
         * Sets the maximum number of concurrent streams allowed per connection.
         */
        default SELF maxConcurrentStreams(long maxConcurrentStreams) {
            addOption(MAX_CONCURRENT_STREAMS, maxConcurrentStreams);
            return self();
        }

        /**
         * Sets the maximum size for HTTP/2 frames.
         */
        default SELF maxFrameSize(int maxFrameSize) {
            addOption(MAX_FRAME_SIZE, maxFrameSize);
            return self();
        }

        /**
         * Sets the maximum size for the HTTP/2 header list.
         */
        default SELF maxHeaderListSize(int maxHeaderListSize) {
            addOption(MAX_HEADER_LIST_SIZE, maxHeaderListSize);
            return self();
        }

    }
}
