package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.util.resoruce.Cleanable;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.constant.Component.Registry.CONSUL;

/**
 * Creates and retrieves {@link RegistryService} instance.
 */
@Extensible(value = CONSUL, scope = APPLICATION)
public interface RegistryFactory extends Cleanable {

    /**
     * Retrieves a {@link RegistryService} instance for the given configuration.
     *
     * @param config the registry configuration
     * @return the {@link RegistryService} instance
     */
    RegistryService getService(RegistryConfig config);
}



