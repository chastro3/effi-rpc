package io.effi.rpc.engine;

import io.effi.rpc.contract.Callee;
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
