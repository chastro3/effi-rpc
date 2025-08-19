package io.effi.rpc.config;

import java.util.List;

/**
 * Represents hierarchical configurations with parent-child relationships.
 * <p>
 * Extends basic configuration capabilities with support for parent configurations
 * and merged value retrieval across the configuration hierarchy.
 */
public interface HierarchicalConfig extends Config {

    /**
     * Retrieves merged values for the given config name.
     */
    <V> List<V> getMerged(ConfigName<V> name);

    /**
     * Retrieves merged values for the given name.
     */
    <V> List<V> getMerged(String name);

    /**
     * Sets the parent configuration.
     *
     * @param parent the parent configuration
     */
    void withParent(Config parent);

    /**
     * Returns the parent configuration.
     */
    Config parent();

    /**
     * Supplies access to the {@link HierarchicalConfig}.
     */
    interface Supplier extends Config.Supplier {

        @Override
        HierarchicalConfig config();

        default <V> List<V> getMergedConfig(ConfigName<V> name) {
            return config().getMerged(name);
        }

        default <V> List<V> getMergedConfig(String name) {
            return config().getMerged(name);
        }
    }
}


