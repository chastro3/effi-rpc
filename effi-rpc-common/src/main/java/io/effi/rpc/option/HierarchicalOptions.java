package io.effi.rpc.option;

import java.util.Map;

/**
 * Defines options with an optional parent and owner.
 */
public interface HierarchicalOptions extends Options {

    /**
     * Creates an empty hierarchical options instance.
     *
     * @return hierarchical options
     */
    static HierarchicalOptions create() {
        return new DefaultHierarchicalOptions();
    }

    /**
     * Creates an empty hierarchical options instance with the expected capacity.
     *
     * @param initialCapacity expected number of options
     * @return hierarchical options
     */
    static HierarchicalOptions create(int initialCapacity) {
        return new DefaultHierarchicalOptions(initialCapacity);
    }

    /**
     * Creates hierarchical options backed by the supplied values.
     *
     * @param items initial option values
     * @return hierarchical options
     */
    static HierarchicalOptions create(Map<String, Object> items) {
        return new DefaultHierarchicalOptions(items);
    }

    /**
     * Assigns the parent options.
     *
     * @param parent parent options
     * @return current hierarchical options instance
     */
    HierarchicalOptions withParent(Options parent);

    /**
     * Returns the parent options.
     */
    Options parent();

    /**
     * Assigns the option owner.
     *
     * @param owner option owner
     * @return current hierarchical options instance
     */
    HierarchicalOptions withOwner(Object owner);

    /**
     * Returns the option owner.
     */
    Object owner();

    /**
     * Supplies access to hierarchical options.
     */
    interface Supplier extends Options.Supplier {

        @Override
        HierarchicalOptions options();
    }
}
