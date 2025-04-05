package io.effi.rpc.common.config;

import java.util.List;

/**
 * Represents a hierarchical configuration that supports retrieving value(s) from parent configuration.
 */
public interface LinkedConfig extends Config {

    /**
     * Retrieves cascaded values for the given config key.
     *
     * @param key the config key
     * @return a list of values, possibly including values from parent configurations
     */
    List<String> getCascaded(ConfigKey key);

    /**
     * Retrieves cascaded values for the given key.
     *
     * @param key the config key
     * @return a list of values, possibly including values from parent configurations
     */
    List<String> getCascaded(String key);

    /**
     * Returns the parent configuration.
     */
    Config parent();

    /**
     * Defines the source priority for retrieving values in a hierarchical configuration.
     */
    enum Source {

        /**
         * Retrieves the value from the current configuration only.
         */
        SELF_ONLY,

        /**
         * Retrieves the value from the current configuration first, otherwise from the parent configuration.
         */
        SELF_PREFERRED,

        /**
         * Retrieves the value from the current configuration first, otherwise from the current configuration.
         */
        PARENT_PREFERRED,

        /**
         * Merges values from the parent and current configuration.
         */
        CASCADED
    }

}
