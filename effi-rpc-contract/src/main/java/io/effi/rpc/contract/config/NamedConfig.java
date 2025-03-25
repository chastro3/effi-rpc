package io.effi.rpc.contract.config;

import io.effi.rpc.common.url.ConfigSource;
import io.effi.rpc.contract.manager.Manager;

/**
 * Configuration with a unique name.
 */
public interface NamedConfig extends ConfigSource, Manager.Key {

    /**
     * Returns the unique name of this configuration.
     *
     * @return the unique name
     */
    String name();

}

