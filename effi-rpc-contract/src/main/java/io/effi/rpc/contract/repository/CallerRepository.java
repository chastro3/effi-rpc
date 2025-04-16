package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link Caller} instances.
 */
public class CallerRepository extends AbstractComponentRepository<Caller<?>> {

    public CallerRepository(EffiRpcModule module) {
        super(module);
    }
}
