package io.effi.rpc.config;

import io.effi.rpc.util.StringUtil;

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
     * Returns the separator for cascading values.
     * Only applies when the strategy is {@link Strategy#CASCADED}.
     */
    default String separator() {
        return StringUtil.empty();
    }

    /**
     * Defines the strategy for retrieving values.
     */
    enum Strategy {

        /**
         * Value from the current configuration only.
         */
        SELF_ONLY,

        /**
         * Value from current configuration first, then parent if not found.
         */
        SELF_PREFERRED,

        /**
         * Value from parent configuration first, then current if not found.
         */
        PARENT_PREFERRED,

        /**
         * Merges values from the parent and current configuration.
         */
        CASCADED
    }

}

