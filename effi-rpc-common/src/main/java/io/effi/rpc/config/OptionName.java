package io.effi.rpc.config;


/**
 * Defines typed configuration names with retrieval strategies and default values.
 * <p>
 * Provides a structured way to represent configuration keys along with their
 * resolution strategies and fallback values for hierarchical configuration systems.
 */
public interface OptionName<V> {

    static <V> OptionName<V> of(String name) {
        return ConfigurableOptionName.nameOf(name);
    }

    static <V> OptionName<V> of(String name, V defaultValue) {
        return ConfigurableOptionName.<V>nameOf(name).defaultValue(defaultValue);
    }

    static <V> OptionName<V> of(String name, Strategy strategy) {
        return ConfigurableOptionName.nameOf(name, strategy);
    }

    static <V> OptionName<V> of(String name, Strategy strategy, V defaultValue) {
        return ConfigurableOptionName.<V>nameOf(name, strategy).defaultValue(defaultValue);
    }

    /**
     * Returns the name.
     */
    String name();

    /**
     * Returns the strategy for retrieving the value.
     */
    Strategy strategy();

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

