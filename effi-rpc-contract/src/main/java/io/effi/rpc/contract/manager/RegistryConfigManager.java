package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link RegistryConfig} instances.
 */
public class RegistryConfigManager extends SharableManager<RegistryConfig> {

    public RegistryConfigManager(EffiRpcModule module) {
        super(module);
    }

}
