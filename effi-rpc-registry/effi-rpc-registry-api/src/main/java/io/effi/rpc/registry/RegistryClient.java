package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.trait.Cleanable;
import io.effi.rpc.trait.Closeable;

import java.util.Collection;
import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages service registrations and discoveries in a registry.
 * <p>
 * Provides asynchronous operations for registering, deregistering,
 * and discovering services in a registry system.
 */
public interface RegistryClient extends ScopedPlatform.Supplier, Closeable {

    /**
     * Resolves the registry client for the supplied configuration.
     *
     * @param config   the registry configuration
     * @param platform the owning platform
     * @return the registry client
     */
    static RegistryClient of(RegistryConfig config, ScopedPlatform platform) {
        return platform.namedExtension(RegistryClient.Factory.class, config.type())
                .fetch(config);
    }

    /**
     * Registers multiple service instances.
     *
     * @param instances the service instances to register
     * @return a future completed when every instance is registered
     */
    default Future<Void> register(Collection<ServiceInstance> instances) {
        return Futures.allOf(instances.stream().map(this::register).toList());
    }

    /**
     * Registers a service into the registry.
     *
     * @param instance the service instance to register
     * @return a future completed when registration finishes
     */
    Future<Void> register(ServiceInstance instance);

    /**
     * Deregisters multiple service instances.
     *
     * @param instances the service instances to deregister
     * @return a future completed when every instance is deregistered
     */
    default Future<Void> deregister(Collection<ServiceInstance> instances) {
        return Futures.allOf(instances.stream().map(this::deregister).toList());
    }

    /**
     * Deregisters a service from the registry.
     *
     * @param instance the service instance to deregister
     * @return a future completed when deregistration finishes
     */
    Future<Void> deregister(ServiceInstance instance);

    /**
     * Discovers services from the registry by service id.
     *
     * @param serviceName the service id to lookup
     * @return a future containing the list of discovered service instances
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

    /**
     * Creates and caches registry clients for registry configurations.
     */
    @Extensible(scope = PLATFORM)
    interface Factory extends Cleanable {

        /**
         * Retrieves a {@link RegistryClient} instance for the given configuration.
         *
         * @param config the registry configuration
         * @return the {@link RegistryClient} instance
         */
        RegistryClient fetch(RegistryConfig config);
    }
}
