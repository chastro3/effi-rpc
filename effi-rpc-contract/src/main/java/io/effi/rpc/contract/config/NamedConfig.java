package io.effi.rpc.contract.config;

import io.effi.rpc.common.config.ConfigSource;
import io.effi.rpc.contract.manager.ComponentManager;

/**
 * Configuration with a unique name.
 */
public interface NamedConfig extends ConfigSource, ComponentManager.Key {

    /**
     * Returns the unique name of this configuration.
     */
    String name();

}

