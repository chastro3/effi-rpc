package io.effi.rpc.protocol.http;

import io.effi.rpc.component.transport.support.TcpEndpointConfig;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;

import static io.effi.rpc.util.AssertUtil.notNull;

public abstract class HttpEndpointConfig extends TcpEndpointConfig {

    public static final OptionName<String> TRACING_POLICY = ConfigurableOptionName.nameOf("tracingPolicy");

    public static final OptionName<Integer> DECODER_INITIAL_BUFFER_SIZE = ConfigurableOptionName.nameOf("decoderInitialBufferSize");

    public static final OptionName<Integer> MAX_MESSAGE_SIZE = ConfigurableOptionName.<Integer>nameOf("maxMessageSize").defaultValue(1024 * 32);

    protected HttpVersion version;

    protected HttpEndpointConfig(HttpVersion version, String id, Options options) {
        super(notNull(version, "version").name(), id, options);
    }

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
            addOption(MAX_MESSAGE_SIZE, maxMessageSize);
            return self();
        }

        /**
         * Set the tracing policy for the server.
         * <p>
         * Controls how tracing information is collected and propagated.
         */
        default SELF tracingPolicy(String tracingPolicy) {
            addOption(TRACING_POLICY, tracingPolicy);
            return self();
        }

        /**
         * Set the initial buffer size for the HTTP decoder.
         * <p>
         * Defines the initial size of the buffer used during HTTP request decoding.
         */
        default SELF decoderInitialBufferSize(int decoderInitialBufferSize) {
            addOption(DECODER_INITIAL_BUFFER_SIZE, decoderInitialBufferSize);
            return self();
        }
    }
}
