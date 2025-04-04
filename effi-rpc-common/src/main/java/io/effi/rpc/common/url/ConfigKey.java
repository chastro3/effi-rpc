package io.effi.rpc.common.url;

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
    Config.Source source();

    /**
     * Returns the default value for this key if no value is found.
     */
    String defaultValue();

}

