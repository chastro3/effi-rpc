package io.effi.rpc.common.url;

/**
 * Represents a key in a configuration.
 * Defines the key name, value source strategy, and default value.
 */
public interface ConfigKey {

    /**
     * Returns the configuration key name.
     *
     * @return the key name
     */
    String key();

    /**
     * Returns the source strategy for retrieving the configuration value.
     *
     * @return the value source
     */
    Config.Source source();

    /**
     * Returns the default value for this key if no value is found.
     *
     * @return the default value
     */
    String defaultValue();

}

