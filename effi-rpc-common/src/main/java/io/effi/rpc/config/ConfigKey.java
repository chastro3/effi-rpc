package io.effi.rpc.config;

/**
 * Represents a configuration key.
 */
public interface ConfigKey {

    /**
     * Returns the key name.
     */
    String key();

    /**
     * Returns the strategy for retrieving the value.
     */
    Strategy strategy();

    /**
     * Returns the default value if not found.
     */
    String defaultValue();

    /**
     * Defines the strategy for retrieving values.
     */
    enum Strategy {

        /**
         * Value from the current configuration only.
         */
        SELF_ONLY,

        /**
         * Value from the current configuration, or parent if not found.
         */
        SELF_PREFERRED,

        /**
         * Value from the current configuration, or parent if not found.
         */
        PARENT_PREFERRED,

        /**
         * Merges values from the parent and current configuration.
         */
        CASCADED
    }

}
