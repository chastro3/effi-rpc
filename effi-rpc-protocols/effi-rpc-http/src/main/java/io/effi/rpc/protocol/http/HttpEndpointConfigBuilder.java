package io.effi.rpc.protocol.http;

import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.config.transport.EndpointConfigBuilder;

/**
 * Builds http {@link EndpointConfig} instance and defines configuration.
 */
public interface HttpEndpointConfigBuilder<T extends EndpointConfig, C extends HttpEndpointConfigBuilder<T, C>> extends EndpointConfigBuilder<T, C> {

    /**
     * Set the max content length for HTTP aggregation.
     * <p>
     * Defines the maximum length of the aggregated HTTP message content.
     * Applies to both HTTP requests and responses.
     */
    default C maxMessageSize(int maxMessageSize) {
        config().set(DefaultConfigNames.MAX_MESSAGE_SIZE, maxMessageSize);
        return returnThis();
    }

    /**
     * Set the tracing policy for the server.
     * <p>
     * Controls how tracing information is collected and propagated.
     */
    default C tracingPolicy(String tracingPolicy) {
        config().set(DefaultConfigNames.TRACING_POLICY, tracingPolicy);
        return returnThis();
    }

    /**
     * Set the initial buffer size for the HTTP decoder.
     * <p>
     * Defines the initial size of the buffer used during HTTP request decoding.
     */
    default C decoderInitialBufferSize(int decoderInitialBufferSize) {
        config().set(DefaultConfigNames.DECODER_INITIAL_BUFFER_SIZE, decoderInitialBufferSize);
        return returnThis();
    }
}
