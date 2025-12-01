package io.effi.rpc.config;

import java.util.List;
import java.util.Map;

/**
 * Represents hierarchical options with parent-child relationships.
 * <p>
 * Extends basic option capabilities with support for parent options
 * and merged value retrieval across the option hierarchy.
 */
public interface HierarchicalOptions extends Options {

    static HierarchicalOptions create() {
        return new DefaultHierarchicalOptions();
    }

    static HierarchicalOptions create(int initialCapacity) {
        return new DefaultHierarchicalOptions(initialCapacity);
    }

    static HierarchicalOptions create(Map<String, Object> items) {
        return new DefaultHierarchicalOptions(items);
    }

    /**
     * Retrieves merged values for the given option name.
     */
    <V> List<V> mergedOption(OptionName<V> name);

    /**
     * Retrieves merged values for the given name.
     */
    <V> List<V> mergedOption(String name);

    /**
     * Sets the owner of the options.
     */
    HierarchicalOptions withOwner(Object owner);

    /**
     * Returns the owner of the options.
     */
    Object owner();

    /**
     * Sets the parent options.
     *
     * @param parent the parent options
     */
    HierarchicalOptions withParent(Options parent);

    /**
     * Returns the parent options.
     */
    Options parent();

    /**
     * Supplies access to the {@link HierarchicalOptions}.
     */
    interface Supplier extends Options.Supplier {

        @Override
        HierarchicalOptions options();

        default <V> List<V> mergedOption(OptionName<V> name) {
            return options().mergedOption(name);
        }

        default <V> List<V> mergedOption(String name) {
            return options().mergedOption(name);
        }
    }
}


