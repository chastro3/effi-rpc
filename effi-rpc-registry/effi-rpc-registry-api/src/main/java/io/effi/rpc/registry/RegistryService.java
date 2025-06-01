package io.effi.rpc.registry;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.URL;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Manages service registrations and discoveries in a registry.
 */
public interface RegistryService extends Closeable {

    /**
     * Registers a service into the registry.
     *
     * @param serviceName the service name
     * @param serviceHost the service host
     */
    CompletableFuture<Void> register(String serviceName, ServiceHost serviceHost);

    /**
     * Deregisters a service from the registry.
     *
     * @param serviceName the service name
     * @param serviceHost the service host
     */
    CompletableFuture<Void> deregister(String serviceName, ServiceHost serviceHost);

    /**
     * Discovers service(s) from the registry.
     *
     * @param serviceName the service name
     * @param module the module
     * @return discovered service URLs
     */
    CompletableFuture<List<URL>> discover(String serviceName, EffiRpcModule module);

    /**
     * Represents a registration action.
     */
    @FunctionalInterface
    interface RegistrationAction {

        /**
         * Executes registration logic with given metadata.
         *
         * @param metaData metadata map
         * @throws Exception if execution fails
         */
        CompletableFuture<Void> execute(Map<String, String> metaData);
    }
}




