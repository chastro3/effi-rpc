package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link RegistryConfig} instances.
 */
public class RegistryConfigRepository extends SharableComponentRepository<RegistryConfig> {

    public RegistryConfigRepository(EffiRpcModule module) {
        super(module);
    }

}
