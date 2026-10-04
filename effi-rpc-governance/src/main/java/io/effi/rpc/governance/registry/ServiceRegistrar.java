package io.effi.rpc.governance.registry;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.trait.Closeable;

import java.util.Collection;

/**
 * Coordinates service-instance registration and deregistration.
 */
public interface ServiceRegistrar extends ScopedApplication.Supplier, Closeable {

    /**
     * Returns the service instances owned by this registrar.
     */
    Collection<ServiceInstance> serviceInstances();

    /**
     * Returns the registry configurations used for registration.
     */
    Collection<RegistryConfig> registryConfigs();

    /**
     * Registers every owned service instance and completes when the batch finishes.
     */
    Future<Void> register();

    /**
     * Deregisters every owned service instance and completes when the batch finishes.
     */
    Future<Void> deregister();

    @Override
    default void close() {
        deregister();
    }
}
