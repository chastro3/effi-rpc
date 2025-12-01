package io.effi.rpc.registry;

import io.effi.rpc.trait.Identifiable;

import java.util.Map;

/**
 * Represents service instances for registration and discovery.
 * <p>
 * Provides a standardized interface for service instance information
 * including identification, network location, and metadata.
 */
public interface ServiceInstance extends Identifiable {

    /**
     * Returns the unique instance ID.
     */
    @Override
    String id();

    /**
     * Returns the logical service id.
     */
    String serviceName();

    /**
     * Returns the protocol used by this instance.
     */
    String protocol();

    /**
     * Returns the host or IP address.
     */
    String host();

    /**
     * Returns the listening port.
     */
    int port();

    /**
     * Returns metadata for routing or extensions.
     */
    Map<String, String> metadata();

    /**
     * Adds metadata to the instance.
     */
    ServiceInstance addMetadata(String key, String value);

    /**
     * Adds multiple metadata to the instance.
     */
    ServiceInstance addMetadata(Map<String, String> metadata);
}

