package io.effi.rpc.config.transport;

import io.effi.rpc.config.DefaultConfigNames;

/**
 * Builds {@link EndpointConfig} instance and defines configuration for TCP.
 */
public interface TcpEndpointConfigBuilder<T extends EndpointConfig, C extends TcpEndpointConfigBuilder<T, C>>
        extends EndpointConfigBuilder<T, C> {

    /**
     * Enable or disable TCP_NO_DELAY (Nagle's algorithm).
     * <p>
     * When {@code true}, disables Nagle to reduce latency by sending small packets immediately.<br>
     * When {@code false}, enables Nagle to reduce packet count, which may increase latency.
     */
    default C noDelay(boolean noDelay) {
        config().set(DefaultConfigNames.NO_DELAY, noDelay);
        return returnThis();
    }

    /**
     * Enable or disable TCP keep-alive.
     */
    default C keepAlive(boolean keepAlive) {
        config().set(DefaultConfigNames.KEEP_ALIVE, keepAlive);
        return returnThis();
    }

    /**
     * Sets the idle count threshold for closing connections.
     */
    default C idleCountThreshold(int ideCountThreshold) {
        config().set(DefaultConfigNames.IDLE_COUNT_THRESHOLD, ideCountThreshold);
        return returnThis();
    }

    /**
     * Sets the interval for triggering idle connections.
     */
    default C idleTriggerInterval(int idleTriggerInterval) {
        config().set(DefaultConfigNames.IDLE_TRIGGER_INTERVAL, idleTriggerInterval);
        return returnThis();
    }

    /**
     * Enable or disable SSL/TLS.
     */
    default C ssl(boolean ssl) {
        config().set(DefaultConfigNames.SSL, ssl);
        return returnThis();
    }

    /**
     * Sets the certificate configuration.
     */
    C certificate(CertificateConfig certificateConfig);
}

