package io.effi.rpc.contract.repository;

import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link ThreadPool} instances.
 */
public class ThreadPoolRepository extends AbstractComponentRepository<ThreadPool> {

    public ThreadPoolRepository(EffiRpcModule module) {
        super(module);
    }
}
