package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.resoruce.Closeable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Exposes the service through a specified {@link EffiRpcPlatform}.
 */
@ScopedComponent(scope = PLATFORM)
public interface ServiceHost extends URL.Provider, EffiRpcPlatform.Provider, Identifiable, Closeable, Comparable<ServiceHost> {

    /**
     * Returns the server configuration.
     */
    ServerConfig serverConfig();

    /**
     * Returns the address where the service is exposed.
     */
    InetSocketAddress exportedAddress();

    /**
     * Registers one or more registry configurations.
     *
     * @param registryConfigs the registry configuration(s) to be registered
     */
    ServiceHost registerAt(RegistryConfig... registryConfigs);

    /**
     * Returns the list of registered registries.
     */
    List<RegistryConfig> registries();

    /**
     * Starts the server and makes the service available for remote calls.
     */
    CompletableFuture<Void> start();

    @Override
    default String id() {
        return "<" + url().protocol() + ">" + url().address();
    }
}



