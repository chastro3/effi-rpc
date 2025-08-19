package io.effi.rpc.component.transport;

import io.effi.rpc.config.Config;
import io.effi.rpc.util.Identifiable;

/**
 * Defines configurations for communication endpoints.
 * <p>
 * Provides a standardized interface for endpoint configuration including
 * protocol, protocol stack, and certificate settings for secure connections.
 */
public interface EndpointConfig extends Config.Supplier, Identifiable {

    /**
     * Returns the protocol name of the endpoint.
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

}

