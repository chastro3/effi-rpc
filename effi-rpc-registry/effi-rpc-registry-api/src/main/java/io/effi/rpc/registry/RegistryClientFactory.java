package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.util.resoruce.Cleanable;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;
import static io.effi.rpc.config.ConfigValues.Registry.CONSUL;

/**
 * Creates and retrieves registry client instances.
 * <p>
 * Provides factory methods for creating registry clients based on
 * registry configurations with application-scoped extensibility.
 */
@Extensible(value = CONSUL, scope = PLATFORM)
public interface RegistryClientFactory extends Cleanable {

    /**
     * Retrieves a {@link RegistryClient} instance for the given configuration.
     *
     * @param config the registry configuration
     * @return the {@link RegistryClient} instance
     */
    RegistryClient fetch(RegistryConfig config);
}



