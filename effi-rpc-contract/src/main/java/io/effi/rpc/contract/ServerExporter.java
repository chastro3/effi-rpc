package io.effi.rpc.contract;

import io.effi.rpc.common.config.URLSource;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.contract.repository.CalleeRepository;
import io.effi.rpc.contract.repository.ComponentRepository;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Exposes the service through a specified {@link EffiRpcModule}.
 */
public interface ServerExporter extends URLSource, ModuleSource, ComponentRepository.Key {

    /**
     * Returns the server configuration.
     */
    ServerConfig serverConfig();

    /**
     * Returns the address where the service is exposed.
     */
    InetSocketAddress exportedAddress();

    /**
     * Registers one or more callees to handle incoming requests.
     *
     * @param callee callees to register
     * @return this exporter instance
     */
    ServerExporter callee(Callee<?>... callee);

    /**
     * Registers one or more registry configurations.
     *
     * @param registryConfigs registries to register
     * @return this exporter instance
     */
    ServerExporter registry(RegistryConfig... registryConfigs);

    /**
     * Returns the list of registered registries.
     */
    List<RegistryConfig> registries();

    /**
     * Returns the exported callee repository.
     */
    CalleeRepository calleeRepository();

    /**
     * Starts the server and makes the service available for remote calls.
     */
    void export();
}



