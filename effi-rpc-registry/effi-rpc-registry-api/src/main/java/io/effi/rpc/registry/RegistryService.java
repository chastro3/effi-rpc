package io.effi.rpc.registry;

import io.effi.rpc.common.util.resoruce.Closeable;
import io.effi.rpc.common.config.URL;

import java.util.List;

/**
 * Manages service registration and discovery in a registry.
 */
public interface RegistryService extends Closeable {

    /**
     * Connects to the registry.
     *
     * @param registryUrl the registry URL
     */
    void connect(URL registryUrl);

    /**
     * Registers a service with the registry.
     *
     * @param exporterUrl the service URL to register
     */
    void register(URL exporterUrl);

    /**
     * Deregisters a service from the registry.
     *
     * @param exporterUrl the service URL to deregister
     */
    void deregister(URL exporterUrl);

    /**
     * Discovers services in the registry.
     *
     * @param requestUrl the service discovery URL
     * @return a list of discovered service URLs
     */
    List<URL> discover(URL requestUrl);
}



