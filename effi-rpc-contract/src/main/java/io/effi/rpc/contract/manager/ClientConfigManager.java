package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link ClientConfig} instances.
 */
public class ClientConfigManager extends AbstractManager<ClientConfig> {

    public ClientConfigManager(EffiRpcModule module) {
        super(module);
    }
}
