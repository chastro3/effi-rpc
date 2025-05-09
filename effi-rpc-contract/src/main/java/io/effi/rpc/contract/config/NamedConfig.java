package io.effi.rpc.contract.config;

import io.effi.rpc.config.ExtParams;
import io.effi.rpc.contract.repository.ComponentRepository;

/**
 * Defines configuration with a unique name.
 */
public interface NamedConfig extends ExtParams, ComponentRepository.Key {

    /**
     * Returns the unique name of this configuration.
     */
    String name();

}

