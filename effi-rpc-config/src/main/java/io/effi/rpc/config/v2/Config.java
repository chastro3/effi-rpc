package io.effi.rpc.config.v2;

import java.util.Map;

/**
 * Configuration management for value retrieval and update.
 */
public interface Config {

    /**
     * Sets the value by config name.
     */
    <V> void set(ConfigName<V> name, V value);

    /**
     * Retrieves the value by config name.
     */
    <V> V get(ConfigName<V> name);

    /**
     * Removes the value by name.
     */
    <V> V remove(ConfigName<V> name);

    /**
     * Returns all config entries.
     */
    Map<String, Object> items();

    /**
     * Returns the owner of the config.
     */
    Object owner();

    /**
     * Provides access to the {@link Config}.
     */
    interface Provider {
        default <V> V getConfig(ConfigName<V> name) {
            return config().get(name);
        }

        /**
         * Returns the associated {@link Config}.
         */
        Config config();
    }
}



