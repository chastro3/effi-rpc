package io.effi.rpc.config;

import java.util.Map;

/**
 * Manages configuration values for retrieval and update.
 * <p>
 * Provides a centralized interface for setting, getting, and removing configuration
 * values by name or typed config name, with support for default values.
 */
public interface Config {

    /**
     * Sets the value by config name.
     */
    <V> void set(ConfigName<V> name, V value);

    /**
     * Sets the value by name.
     */
    <V> void set(String name, V value);

    /**
     * Retrieves the value by config name.
     */
    <V> V get(ConfigName<V> name);

    /**
     * Retrieves the value by name.
     */
    <V> V get(String name);

    /**
     * Removes the value by name.
     */
    <V> V remove(ConfigName<V> name);

    /**
     * Removes the value by name.
     */
    <V> V remove(String name);

    /**
     * Returns all config entries.
     */
    Map<String, Object> items();

    /**
     * Sets the owner of the config.
     */
    Config withOwner(Object owner);

    /**
     * Returns the owner of the config.
     */
    Object owner();

    default <V> V get(ConfigName<V> name, V defaultValue) {
        V value = get(name);
        return value != null ? value : defaultValue;
    }

    default <V> V get(String name, V defaultValue) {
        V value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Supplies access to the {@link Config}.
     */
    interface Supplier {

        /**
         * Returns the associated {@link Config}.
         */
        Config config();

        default <V> V getConfig(ConfigName<V> name) {
            return config().get(name);
        }

        default <V> V getConfig(ConfigName<V> name, V defaultValue) {
            return config().get(name, defaultValue);
        }

        default <V> V getConfig(String name) {
            return config().get(name);
        }

        default <V> V getConfig(String name, V defaultValue) {
            return config().get(name, defaultValue);
        }
    }

}



