package io.effi.rpc.contract.module;

/**
 * Provides access to a {@link EffiRpcModule} instance.
 */
public interface ModuleSource {

    /**
     * Returns the associated {@link Config}.
     */
    EffiRpcModule module();
}
