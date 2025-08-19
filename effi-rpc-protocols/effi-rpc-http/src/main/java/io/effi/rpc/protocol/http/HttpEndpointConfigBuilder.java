package io.effi.rpc.protocol.http;

import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.EndpointConfigBuilder;

/**
 * Builds http {@link EndpointConfig} instance and defines configuration.
 */
public interface HttpEndpointConfigBuilder<T extends EndpointConfig, SELF extends HttpEndpointConfigBuilder<T, SELF>> extends EndpointConfigBuilder<T, SELF> {

    /**
     * Set the max content length for HTTP aggregation.
     * <p>
     * Defines the maximum length of the aggregated HTTP message content.
     * Applies to both HTTP requests and responses.
     */
    default SELF maxMessageSize(int maxMessageSize) {
        config().set(ConfigNames.MAX_MESSAGE_SIZE, maxMessageSize);
        return self();
    }

    /**
     * Set the tracing policy for the server.
     * <p>
     * Controls how tracing information is collected and propagated.
     */
    default SELF tracingPolicy(String tracingPolicy) {
        config().set(ConfigNames.TRACING_POLICY, tracingPolicy);
        return self();
    }

    /**
     * Set the initial buffer size for the HTTP decoder.
     * <p>
     * Defines the initial size of the buffer used during HTTP request decoding.
     */
    default SELF decoderInitialBufferSize(int decoderInitialBufferSize) {
        config().set(ConfigNames.DECODER_INITIAL_BUFFER_SIZE, decoderInitialBufferSize);
        return self();
    }
}
