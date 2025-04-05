package io.effi.rpc.common.config;

/**
 * Represents a key in a configuration.
 */
public interface ConfigKey {

    /**
     * Returns the configuration key name.
     */
    String key();

    /**
     * Returns the source strategy for retrieving the configuration value.
     */
    LinkedConfig.Source source();

    /**
     * Returns the default value for this key if no value is found.
     */
    String defaultValue();

}