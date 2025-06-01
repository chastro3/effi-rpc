package io.effi.rpc.config;

import io.effi.rpc.util.Identifiable;

/**
 * Defines configuration with a unique name.
 */
public interface NamedConfig extends ExtParams, Identifiable {

    /**
     * Returns the unique name of this configuration.
     */
    String name();

}

