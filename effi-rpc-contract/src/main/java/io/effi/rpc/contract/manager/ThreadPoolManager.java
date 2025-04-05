package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link ThreadPool} instances.
 */
public class ThreadPoolManager extends AbstractComponentManager<ThreadPool> {

    public ThreadPoolManager(EffiRpcModule module) {
        super(module);
    }
}
