package io.effi.rpc.config;

import io.effi.rpc.util.StringUtil;

import java.util.Map;

/**
 * Configuration management for value retrieval and update.
 */
public interface Config {

    /**
     * Sets the value by config key.
     */
    void set(ConfigName key, String value);

    /**
     * Retrieves the value by string key.
     */
    String get(String key);

    /**
     * Retrieves the value or returns the default.
     */
    String getOrDefault(String key, String defaultValue);

    default String[] split(ConfigName key) {
        String value = get(key);
        String separator = key.separator();
        return (StringUtil.isBlank(value) || StringUtil.isBlank(separator))
                ? StringUtil.emptyArray()
                : value.split(key.separator());
    }

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

    /**
     * Retrieves the value by config key.
     */
    String get(ConfigName key);

    default void set(ConfigName key, Object value) {
        set(key.realName(), value);
    }

    default void set(String key, Object value) {
        if (value == null) return;
        if (value instanceof Number || value instanceof Character || value instanceof CharSequence || value instanceof Boolean) {
            set(key, value.toString());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    /**
     * Provides access to the {@link Config}.
     */
    interface Provider {
        default String getConfig(ConfigName key) {
            return config().get(key);
        }

        /**
         * Returns the associated {@link Config}.
         */
        Config config();

        default String[] splitConfig(ConfigName key) {
            return config().split(key);
        }
    }
}



