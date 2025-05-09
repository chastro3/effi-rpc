package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manages the registration and retrieval of {@link ClientConfig} instances.
 */
public class ClientConfigRepository extends AbstractComponentRepository<ClientConfig> {

    public ClientConfigRepository(EffiRpcModule module) {
        super(module);
    }
}
