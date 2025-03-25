package io.effi.rpc.registry;

import io.effi.rpc.common.extension.resoruce.Closeable;
import io.effi.rpc.common.url.URL;

import java.util.List;

/**
 * Handles service registration and discovery within a registry.
 */
public interface RegistryService extends Closeable {

    /**
     * Connects to the specified registry.
     *
     * @param registryUrl the URL of the registry to connect to
     */
    void connect(URL registryUrl);

    /**
     * Registers the specified URL with the registry.
     *
     * @param exporterUrl the URL to register
     */
    void register(URL exporterUrl);

    /**
     * Deregisters the specified URL from the registry.
     *
     * @param exporterUrl the URL to deregister
     */
    void deregister(URL exporterUrl);

    /**
     * Discovers services from the registry using the specified URL.
     *
     * @param requestUrl the URL used for service discovery
     * @return a list of discovered services
     */
    List<URL> discover(URL requestUrl);
}


