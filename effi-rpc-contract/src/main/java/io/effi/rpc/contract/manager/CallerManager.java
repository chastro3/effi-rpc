package io.effi.rpc.contract.manager;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Manage the registration and retrieval of {@link Caller} instances.
 */
public class CallerManager extends AbstractComponentManager<Caller<?>> {

    public CallerManager(EffiRpcModule module) {
        super(module);
    }
}
