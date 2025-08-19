package io.effi.rpc.component.registry;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.Config;
import io.effi.rpc.util.Identifiable;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configurations for registry clients and operations.
 * <p>
 * Provides a standardized interface for registry configuration including
 * type, address, and thread pool settings for registry operations.
 */
@ScopedComponent(scope = PLATFORM)
public interface RegistryConfig extends Config.Supplier, Identifiable, TagComponent {

    /**
     * Return the registry type.
     */
    String type();

    /**
     * Returns the registry address or a list of addresses.
     * If multiple addresses are specified, they must be separated by ','.
     */
    String address();

    /**
     * Returns the thread pool for registry operations.
     */
    ThreadPool threadPool();
}
