package io.effi.rpc.contract.config;

import io.effi.rpc.common.config.ConfigSource;
import io.effi.rpc.contract.repository.ComponentRepository;

/**
 * Configuration with a unique name.
 */
public interface NamedConfig extends ConfigSource, ComponentRepository.Key {

    /**
     * The unique name of this configuration.
     */
    String name();

}

