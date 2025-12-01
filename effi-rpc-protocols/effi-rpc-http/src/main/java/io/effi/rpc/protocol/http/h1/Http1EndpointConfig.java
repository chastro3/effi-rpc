package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;
import io.effi.rpc.protocol.http.HttpEndpointConfig;
import io.effi.rpc.protocol.http.HttpVersion;

public abstract class Http1EndpointConfig extends HttpEndpointConfig {

    public static final OptionName<Integer> MAX_CHUNK_SIZE = ConfigurableOptionName.nameOf("maxChunkSize");

    public static final OptionName<Integer> MAX_INITIAL_LINE_LENGTH = ConfigurableOptionName.nameOf("maxInitialLineLength");

    public static final OptionName<Integer> MAX_HEADER_SIZE = ConfigurableOptionName.nameOf("maxHeaderSize");

    protected HttpVersion version;

    protected Http1EndpointConfig(String id, Options options) {
        super(Http1Protocol.VERSION, id, options);
    }

    public HttpVersion protocolVersion() {
        return version;
    }

    public interface Configurator<SELF extends Configurator<SELF>> extends HttpEndpointConfig.Configurator<SELF> {

        /**
         * Set the maximum length of the initial request line.
         */
        default SELF maxInitialLineLength(int maxInitialLineLength) {
            addOption(MAX_INITIAL_LINE_LENGTH, maxInitialLineLength);
            return self();
        }

        /**
         * Set the maximum header size for HTTP requests.
         */
        default SELF maxHeaderSize(int maxHeaderSize) {
            addOption(MAX_HEADER_SIZE, maxHeaderSize);
            return self();
        }

        /**
         * Set the maximum size of a single chunk in chunked transfer encoding.
         */
        default SELF maxChunkSize(int maxChunkSize) {
            addOption(MAX_CHUNK_SIZE, maxChunkSize);
            return self();
        }
    }
}
