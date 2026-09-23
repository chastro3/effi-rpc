package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.option.Options;
import io.effi.rpc.protocol.http.HttpEndpointConfig;
import io.effi.rpc.protocol.http.HttpVersion;

public abstract class Http1EndpointConfig extends HttpEndpointConfig {

    protected HttpVersion version;

    protected Http1EndpointConfig(String id, Options options, CertificateConfig certificateConfig) {
        super(Http1Protocol.VERSION, id, options, certificateConfig);
    }

    public HttpVersion protocolVersion() {
        return version;
    }

    public interface Configurator<SELF extends Configurator<SELF>> extends HttpEndpointConfig.Configurator<SELF> {

        /**
         * Set the maximum length of the initial request line.
         */
        default SELF maxInitialLineLength(int maxInitialLineLength) {
            addOption(Http1Options.MAX_INITIAL_LINE_LENGTH, maxInitialLineLength);
            return self();
        }

        /**
         * Set the maximum header size for HTTP requests.
         */
        default SELF maxHeaderSize(int maxHeaderSize) {
            addOption(Http1Options.MAX_HEADER_SIZE, maxHeaderSize);
            return self();
        }

        /**
         * Set the maximum size of a single chunk in chunked transfer encoding.
         */
        default SELF maxChunkSize(int maxChunkSize) {
            addOption(Http1Options.MAX_CHUNK_SIZE, maxChunkSize);
            return self();
        }
    }
}
