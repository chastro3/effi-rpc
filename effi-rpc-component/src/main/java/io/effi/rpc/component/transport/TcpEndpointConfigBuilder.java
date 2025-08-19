package io.effi.rpc.component.transport;

import io.effi.rpc.config.ConfigNames;

/**
 * Builds {@link EndpointConfig} instance and defines configuration for TCP.
 */
public interface TcpEndpointConfigBuilder<T extends EndpointConfig, SELF extends TcpEndpointConfigBuilder<T, SELF>>
        extends EndpointConfigBuilder<T, SELF> {

    /**
     * Enable or disable TCP_NO_DELAY (Nagle's algorithm).
     * <p>
     * When {@code true}, disables Nagle to reduce latency by sending small packets immediately.<br>
     * When {@code false}, enables Nagle to reduce packet count, which may increase latency.
     */
    default SELF noDelay(boolean noDelay) {
        config().set(ConfigNames.TCP_NO_DELAY, noDelay);
        return self();
    }

    /**
     * Enable or disable TCP keep-alive.
     */
    default SELF keepAlive(boolean keepAlive) {
        config().set(ConfigNames.TCP_KEEP_ALIVE, keepAlive);
        return self();
    }

    /**
     * Sets the idle count threshold for closing connections.
     */
    default SELF idleCountThreshold(int ideCountThreshold) {
        config().set(ConfigNames.IDLE_COUNT_THRESHOLD, ideCountThreshold);
        return self();
    }

    /**
     * Sets the interval for triggering idle connections.
     */
    default SELF idleTriggerInterval(int idleTriggerInterval) {
        config().set(ConfigNames.IDLE_TRIGGER_INTERVAL, idleTriggerInterval);
        return self();
    }

    /**
     * Enables or disable SSL/TLS.
     */
    default SELF ssl(boolean ssl) {
        config().set(ConfigNames.TCP_SSL, ssl);
        return self();
    }

    /**
     * Sets the certificate configuration.
     */
    SELF certificate(CertificateConfig certificateConfig);
}

