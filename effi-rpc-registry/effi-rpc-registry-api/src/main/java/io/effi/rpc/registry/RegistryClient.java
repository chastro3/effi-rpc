package io.effi.rpc.registry;

import io.effi.rpc.async.Future;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.util.resoruce.Closeable;

import java.util.List;

/**
 * Manages service registrations and discoveries in a registry.
 * <p>
 * Provides asynchronous operations for registering, deregistering,
 * and discovering services in a registry system.
 * </p>
 */
public interface RegistryClient extends ScopedPlatform.Supplier, Closeable {

    /**
     * Registers a service into the registry.
     *
     * @param instance the service instance to register
     */
    Future<Void> register(ServiceInstance instance);

    /**
     * Deregisters a service from the registry.
     *
     * @param instance the service instance to deregister
     */
    Future<Void> deregister(ServiceInstance instance);

    /**
     * Discovers services from the registry by service name.
     *
     * @param serviceName the service name to lookup
     * @return a CompletableFuture containing the list of discovered service instances
     */
    Future<List<ServiceInstance>> lookup(String serviceName);

    /**
     * Represents a registration process responsible for registering a service instance.
     */
    @FunctionalInterface
    interface Registration {

        /**
         * Executes the registration of the specified service instance.
         *
         * @param instance the service instance to register
         */
        Future<Void> register(ServiceInstance instance);
    }
}




