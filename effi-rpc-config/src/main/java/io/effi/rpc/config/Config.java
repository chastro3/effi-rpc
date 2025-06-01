package io.effi.rpc.config;

import java.util.Map;

/**
 * Configuration management for value retrieval and update.
 */
public interface Config {

    /**
     * Retrieves the value by config key.
     */
    String get(ConfigKey key);

    /**
     * Retrieves the value by string key.
     */
    String get(String key);

    /**
     * Retrieves the value or returns the default.
     */
    String getOrDefault(String key, String defaultValue);

    /**
     * Sets the value by config key.
     */
    void set(ConfigKey key, String value);

    /**
     * Sets the value by string key.
     */
    void set(String key, String value);

    /**
     * Sets multiple config entries.
     */
    void set(Map<String, String> items);

    /**
     * Removes the value by key.
     */
    void remove(String key);

    /**
     * Returns all config entries.
     */
    Map<String, String> items();

    /**
     * Returns the owner of the config.
     */
    Object owner();

    default void set(ConfigKey key, Object value) {
        set(key.key(), value);
    }

    default void set(String key, Object value) {
        if (value == null) return;
        if (value instanceof Number || value instanceof Character || value instanceof CharSequence || value instanceof Boolean) {
            set(key, value.toString());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }
}



