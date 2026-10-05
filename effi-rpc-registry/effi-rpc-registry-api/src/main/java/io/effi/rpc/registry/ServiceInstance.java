package io.effi.rpc.registry;

import io.effi.rpc.trait.Identifiable;

import java.util.Map;

/**
 * Represents a service instance registered for discovery.
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
     * Adds one metadata entry to the instance.
     *
     * @param key   metadata key
     * @param value metadata value
     * @return this instance
     */
    ServiceInstance addMetadata(String key, String value);

    /**
     * Adds metadata entries to the instance.
     *
     * @param metadata metadata entries
     * @return this instance
     */
    ServiceInstance addMetadata(Map<String, String> metadata);
}

