package io.effi.rpc.protocol.http;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.support.TcpEndpointConfig;
import io.effi.rpc.option.Options;

import static io.effi.rpc.util.AssertUtil.notNull;

/**
 * Defines common endpoint configuration shared by HTTP protocol versions.
 */
public abstract class HttpEndpointConfig extends TcpEndpointConfig {

    protected HttpVersion version;

    protected HttpEndpointConfig(
            HttpVersion version,
            String id,
            Options options,
            CertificateConfig certificateConfig
    ) {
        super(notNull(version, "version").name(), id, options, certificateConfig);
    }

    /**
     * Returns the configured HTTP protocol version.
     */
    public HttpVersion protocolVersion() {
        return version;
    }

    public interface Configurator<SELF extends Configurator<SELF>> extends TcpEndpointConfig.Configurator<SELF> {

        /**
         * Set the max content length for HTTP aggregation.
         * <p>
         * Defines the maximum length of the aggregated HTTP message content.
         * Applies to both HTTP requests and responses.
         */
        default SELF maxMessageSize(int maxMessageSize) {
            addOption(HttpOptions.MAX_MESSAGE_SIZE, maxMessageSize);
            return self();
        }

        /**
         * Set the initial buffer size for the HTTP decoder.
         * <p>
         * Defines the initial size of the buffer used during HTTP request decoding.
         */
        default SELF decoderInitialBufferSize(int decoderInitialBufferSize) {
            addOption(HttpOptions.DECODER_INITIAL_BUFFER_SIZE, decoderInitialBufferSize);
            return self();
        }
    }
}
