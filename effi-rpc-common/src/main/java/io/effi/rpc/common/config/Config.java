package io.effi.rpc.common.config;

import java.util.Map;

/**
 * Represents a configuration management system that supports
 * retrieving and setting configuration values.
 *
 * @see ConfigKey
 */
public interface Config {

    /**
     * Retrieves the value associated with the given config key.
     *
     * @param key the config key
     * @return the value or {@code null} if not found
     */
    String get(ConfigKey key);

    /**
     * Retrieves the value associated with the given string key.
     *
     * @param key the config key
     * @return the value or {@code null} if not found
     */
    String get(String key);

    /**
     * Retrieves the value for the given key, or returns the default if not found.
     *
     * @param key          the config key
     * @param defaultValue the default value
     * @return the value or default
     */
    String getOrDefault(String key, String defaultValue);

    /**
     * Sets the value for the given config key.
     *
     * @param key   the config key
     * @param value the value
     */
    void set(ConfigKey key, String value);

    /**
     * Sets the value for the given string key.
     *
     * @param key   the config key
     * @param value the value
     */
    void set(String key, String value);

    /**
     * Sets multiple config values from a map.
     *
     * @param items the map of key-value pairs
     */
    void set(Map<String, String> items);

    /**
     * Removes the value for the given key.
     *
     * @param key the config key
     */
    void remove(String key);

    /**
     * Gets the map of all config items.
     */
    Map<String, String> items();

    /**
     * Returns the owner of the current configuration.
     */
    Object owner();

}

