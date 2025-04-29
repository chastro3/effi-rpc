package io.effi.rpc.util;

/**
 * Builds an instance of {@link T} with method chaining support.
 *
 * @param <T> the type of object to build
 * @param <C> the self type for fluent chaining
 */
public interface ChainBuilder<T, C extends ChainBuilder<T, C>> extends Builder<T> {

    /**
     * Returns this instance for method chaining.
     */
    @SuppressWarnings("unchecked")
    default C returnThis() {
        return (C) this;
    }
}


