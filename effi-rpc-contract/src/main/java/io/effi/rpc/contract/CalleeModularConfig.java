package io.effi.rpc.contract;

import io.effi.rpc.contract.module.EffiRpcModule;

/**
 * Configures callee-related modular components.
 */
public class CalleeModularConfig extends InvokerModularConfig<Callee<?>> {

    public CalleeModularConfig(EffiRpcModule module, Callee<?> callee) {
        super(module, callee);
        addConfiguredFilters();
    }

}
