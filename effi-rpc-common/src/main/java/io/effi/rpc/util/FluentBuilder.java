package io.effi.rpc.util;

/**
 * Builds instances of specified types with fluent method chaining.
 * <p>
 * Extends the basic builder pattern with self-returning methods
 * to enable fluent API usage and method chaining.
 */
public interface FluentBuilder<T, SELF extends FluentBuilder<T, SELF>> extends Builder<T> {

    /**
     * Returns this instance.
     */
    @SuppressWarnings("unchecked")
    default SELF self() {
        return (SELF) this;
    }
}


