package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.transport.TransportProtocol;

/**
 * Represents an endpoint with configuration and protocol access.
 */
public interface Endpoint extends ScopedPlatform.Supplier, TransportProtocol.Supplier, Closeable {

    /**
     * Returns the configuration of this endpoint.
     */
    EndpointConfig config();

}



