package io.effi.rpc.contract.module;

import io.effi.rpc.common.url.Config;

/**
 * Provides access to a {@link EffiRpcModule} instance.
 */
public interface ModuleSource {

    /**
     * Returns the associated {@link Config}.
     */
    EffiRpcModule module();
}
