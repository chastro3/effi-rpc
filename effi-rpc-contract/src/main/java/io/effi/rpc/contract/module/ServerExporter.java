package io.effi.rpc.contract.module;

import io.effi.rpc.common.url.URLSource;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.manager.CalleeManager;
import io.effi.rpc.contract.manager.Manager;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Exposes the service through a specified {@link EffiRpcModule}.
 */
public interface ServerExporter extends URLSource, ModuleSource, Manager.Key {

    /**
     * Returns the server configuration associated with this exporter.
     *
     * @return the {@link ServerConfig} containing server configuration details
     */
    ServerConfig serverConfig();

    /**
     * Returns the address through which the service is exposed.
     *
     * @return the {@link InetSocketAddress} representing the exported address
     */
    InetSocketAddress exportedAddress();

    /**
     * Registers one or more {@link Callee} objects to handle requests.
     *
     * @param callee the {@link Callee} objects to register
     * @return the current {@link ServerExporter} instance for chaining
     */
    ServerExporter callee(Callee<?>... callee);

    /**
     * Registers one or more {@link RegistryConfig} objects with the exporter.
     *
     * @param registryConfigs the {@link RegistryConfig} objects to register
     * @return the current {@link ServerExporter} instance for chaining
     */
    ServerExporter registry(RegistryConfig... registryConfigs);

    /**
     * Returns the list of registered {@link RegistryConfig} objects.
     *
     * @return the list of {@link RegistryConfig} associated with this exporter
     */
    List<RegistryConfig> registries();

    /**
     * Returns the {@link CalleeManager} responsible for managing the lifecycle
     * and operations of the exported callee.
     *
     * @return the {@link CalleeManager}
     */
    CalleeManager calleeManager();

    /**
     * Exports the service and starts the server, making it available for remote calls.
     */
    void export();
}


