package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.trait.Cleanable;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.concurrent.Future;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages service registrations and discoveries in a registry.
 * <p>
 * Provides asynchronous operations for registering, deregistering,
 * and discovering services in a registry system.
 * </p>
 */
public interface RegistryClient extends ScopedPlatform.Supplier, Closeable {

    static RegistryClient of(RegistryConfig config, ScopedPlatform platform) {
        return platform.namedExtension(RegistryClient.Factory.class, config.type())
                .fetch(config);
    }

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
     * Discovers services from the registry by service id.
     *
     * @param serviceName the service id to lookup
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

    /**
     * Creates and retrieves registry client instances.
     * <p>
     * Provides factory methods for creating registry clients based on
     * registry configurations with application-scoped extensibility.
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




