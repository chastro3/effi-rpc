package io.effi.rpc.component;


/**
 * Declares ownership by a {@link ScopedContext} and provides helper methods for scoped operations.
 */
public interface ScopedContextOwned {

    default void setOwner(ScopedContext owner) {
    }

    default ScopedComponentDescriptor ensureScopedComponentDescriptor(Class<?> type) {
        return ScopedContext.ensureScopedComponentDescriptor(type, owner());
    }

    /**
     * Returns the owning {@link ScopedContext}.
     */
    ScopedContext owner();

    default ScopedComponentDescriptor getScopedComponentDescriptor(Class<?> type) {
        return ScopedContext.getScopedComponentDescriptor(type);
    }
}

