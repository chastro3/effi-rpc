package io.effi.rpc.component.transport;

import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.option.Options;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.trait.Fluent;
import io.effi.rpc.trait.Identifiable;

/**
 * Defines configurations for communication endpoints.
 * <p>
 * Provides a standardized interface for endpoint configuration including
 * protocol, protocol stack, and certificate settings for secure connections.
 */
public interface EndpointConfig extends Options.Supplier, Identifiable {

    /**
     * Returns the protocol id of the endpoint.
     */
    String protocolName();

    /**
     * Returns the protocol stack of the endpoint.
     */
    ProtocolStack protocolStack();

    /**
     * Returns the certificate configuration of the endpoint.
     */
    CertificateConfig certificateConfig();


    interface Configurator<SELF extends Configurator<SELF>> extends Options.Supplier, Fluent<SELF> {

        /**
         * Set the send buffer size.
         */
        default SELF sendBufferSize(int sendBufferSize) {
            addOption(TransportOptions.SEND_BUFFER_SIZE, sendBufferSize);
            return self();
        }

        /**
         * Set the receive buffer size.
         */
        default SELF receiveBufferSize(int receiveBufferSize) {
            addOption(TransportOptions.RECEIVE_BUFFER_SIZE, receiveBufferSize);
            return self();
        }

        /**
         * Sets the idle count threshold for closing connections.
         */
        default SELF idleCountThreshold(int ideCountThreshold) {
            addOption(TransportOptions.IDLE_COUNT_THRESHOLD, ideCountThreshold);
            return self();
        }

        /**
         * Sets the interval for triggering idle connections.
         */
        default SELF idleTriggerInterval(int idleTriggerInterval) {
            addOption(TransportOptions.IDLE_TRIGGER_INTERVAL, idleTriggerInterval);
            return self();
        }

        /**
         * Sets the certificate configuration.
         */
        SELF certificate(CertificateConfig certificateConfig);

    }

    /**
     * Builds endpoint configurations with non-option transport fields.
     *
     * @param <T> built endpoint configuration type
     * @param <SELF> concrete builder type
     */
    abstract class Builder<T extends EndpointConfig, SELF extends Builder<T, SELF>>
            extends IdentifiableConfig.Builder<T, SELF>
            implements Configurator<SELF> {

        protected CertificateConfig certificateConfig;

        @Override
        public SELF certificate(CertificateConfig certificateConfig) {
            this.certificateConfig = certificateConfig;
            return self();
        }
    }

}

