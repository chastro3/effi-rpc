package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.option.Options;
import io.effi.rpc.protocol.http.HttpEndpointConfig;

public abstract class Http2EndpointConfig extends HttpEndpointConfig {

    protected Http2EndpointConfig(String id, Options options, CertificateConfig certificateConfig) {
        super(Http2Protocol.VERSION, id, options, certificateConfig);
    }

    public interface Configurator<SELF extends Configurator<SELF>> extends HttpEndpointConfig.Configurator<SELF> {

        /**
         * Sets the size of the HPACK header compression table.
         */
        default SELF headerTableSize(long headerTableSize) {
            addOption(Http2Options.HEADER_TABLE_SIZE, headerTableSize);
            return self();
        }

        /**
         * Sets the initial window size for HTTP/2 flow control.
         */
        default SELF initialWindowSize(int initialWindowSize) {
            addOption(Http2Options.INITIAL_WINDOW_SIZE, initialWindowSize);
            return self();
        }

        /**
         * Sets the maximum number of concurrent streams allowed per connection.
         */
        default SELF maxConcurrentStreams(long maxConcurrentStreams) {
            addOption(Http2Options.MAX_CONCURRENT_STREAMS, maxConcurrentStreams);
            return self();
        }

        /**
         * Sets the maximum size for HTTP/2 frames.
         */
        default SELF maxFrameSize(int maxFrameSize) {
            addOption(Http2Options.MAX_FRAME_SIZE, maxFrameSize);
            return self();
        }

        /**
         * Sets the maximum size for the HTTP/2 header list.
         */
        default SELF maxHeaderListSize(int maxHeaderListSize) {
            addOption(Http2Options.MAX_HEADER_LIST_SIZE, maxHeaderListSize);
            return self();
        }

    }
}
