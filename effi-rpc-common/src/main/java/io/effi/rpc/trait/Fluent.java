package io.effi.rpc.trait;

/**
 * Represents a type that supports fluent-style method chaining.
 * <p>
 * Provides a common base for interfaces and classes using recursive
 * generics, allowing implementations to return their own type through
 * {@link #self()} to enable fluent APIs.
 */
public interface Fluent<SELF extends Fluent<SELF>> {

    /**
     * Returns this instance as its own generic type.
     */
    @SuppressWarnings("unchecked")
    default SELF self() {
        return (SELF) this;
    }
}


