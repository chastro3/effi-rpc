package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.trait.Closeable;

/**
 * Represents an endpoint with host, port, and address details.
 * <p>
 * Provides a standardized interface for network endpoints with
 * configuration and protocol support within a scoped platform.
 */
public interface Endpoint extends ScopedPlatform.Supplier, TransportProtocol.Supplier, Closeable {

    /**
     * Returns the configuration of this endpoint.
     */
    EndpointConfig config();

}



