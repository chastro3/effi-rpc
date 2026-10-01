package io.effi.rpc.component;

/**
 * Declares ownership by a {@link ScopedContext} and provides scoped operations.
 * <p>
 * Enables objects to be associated with a specific scoped context and
 * provides helper methods for component descriptor validation.
 */
public interface ScopedContextOwned {

    /**
     * Returns the owning {@link ScopedContext}.
     */
    ScopedContext owner();

    /**
     * Rebinds this owner-aware object to the supplied scoped context.
     *
     * @param owner new scoped context owner
     */
    default void withOwner(ScopedContext owner) {
    }

    /**
     * Validates and returns the component descriptor for the supplied type.
     *
     * @param type component type
     * @return component descriptor
     */
    default ComponentDescriptor ensureComponentDescriptor(Class<?> type) {
        return ComponentDescriptor.ensure(type, owner());
    }
}

