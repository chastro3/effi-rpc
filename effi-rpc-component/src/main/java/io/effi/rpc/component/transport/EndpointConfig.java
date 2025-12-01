package io.effi.rpc.component.transport;

import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;
import io.effi.rpc.trait.Fluent;
import io.effi.rpc.trait.Identifiable;

/**
 * Defines configurations for communication endpoints.
 * <p>
 * Provides a standardized interface for endpoint configuration including
 * protocol, protocol stack, and certificate settings for secure connections.
 */
public interface EndpointConfig extends Options.Supplier, Identifiable {

    OptionName<Integer> SEND_BUFFER_SIZE = OptionName.of("sendBufferSize");

    OptionName<Integer> RECEIVE_BUFFER_SIZE = OptionName.of("receiveBufferSize");

    OptionName<Integer> IDLE_COUNT_THRESHOLD = OptionName.of("idleCountThreshold", 6);

    OptionName<Integer> IDLE_TRIGGER_INTERVAL = OptionName.of("idleTriggerInterval", 5000);

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
            addOption(SEND_BUFFER_SIZE, sendBufferSize);
            return self();
        }

        /**
         * Set the receive buffer size.
         */
        default SELF receiveBufferSize(int receiveBufferSize) {
            addOption(RECEIVE_BUFFER_SIZE, receiveBufferSize);
            return self();
        }

        /**
         * Sets the idle count threshold for closing connections.
         */
        default SELF idleCountThreshold(int ideCountThreshold) {
            addOption(IDLE_COUNT_THRESHOLD, ideCountThreshold);
            return self();
        }

        /**
         * Sets the interval for triggering idle connections.
         */
        default SELF idleTriggerInterval(int idleTriggerInterval) {
            addOption(IDLE_TRIGGER_INTERVAL, idleTriggerInterval);
            return self();
        }

        /**
         * Sets the certificate configuration.
         */
        default SELF certificate(CertificateConfig certificateConfig) {
            addOption(CertificateConfig.NAME, certificateConfig);
            return self();
        }

    }

}

