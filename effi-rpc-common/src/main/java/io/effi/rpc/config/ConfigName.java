package io.effi.rpc.config;

/**
 * Defines typed configuration names with retrieval strategies and default values.
 * <p>
 * Provides a structured way to represent configuration keys along with their
 * resolution strategies and fallback values for hierarchical configuration systems.
 */
public interface ConfigName<V> {

    /**
     * Returns the name.
     */
    String name();

    /**
     * Returns the strategy for retrieving the value.
     */
    Strategy strategy();

    /**
     * Returns whether the value can be null.
     */
    boolean nullable();

    /**
     * Returns the default value if not found.
     */
    V defaultValue();

    /**
     * Defines the strategy for retrieving values.
     */
    enum Strategy {

        /**
         * Value from the current configuration only.
         */
        ONLY_CURRENT,

        /**
         * Value from current configuration first, then parent if not found.
         */
        CURRENT_FIRST,

        /**
         * Value from parent configuration first, then current if not found.
         */
        PARENT_FIRST,

        /**
         * Merges values from the parent and current configuration.
         */
        MERGE_PARENT
    }

}

