package io.effi.rpc.common.config;

import java.util.Map;

/**
 * Configuration management supporting value retrieval and setting.
 */
public interface Config {

    /**
     * Retrieves the value for the given config key.
     */
    String get(ConfigKey key);

    /**
     * Retrieves the value for the given string key.
     */
    String get(String key);

    /**
     * Retrieves the value or default if not found.
     */
    String getOrDefault(String key, String defaultValue);

    /**
     * Sets the value for the given config key.
     */
    void set(ConfigKey key, String value);

    /**
     * Sets the value for the given string key.
     */
    void set(String key, String value);

    /**
     * Sets multiple config values.
     */
    void set(Map<String, String> items);

    /**
     * Removes the value for the given key.
     */
    void remove(String key);

    /**
     * Returns all config items.
     */
    Map<String, String> items();

    /**
     * Returns the owner of the configuration.
     */
    Object owner();
}


