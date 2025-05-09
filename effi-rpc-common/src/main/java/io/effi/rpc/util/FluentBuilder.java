package io.effi.rpc.util;

/**
 * Builds an instance of {@link T} with fluent method chaining support.
 */
public interface FluentBuilder<T, C extends FluentBuilder<T, C>> extends Builder<T> {

    /**
     * Returns this instance.
     */
    @SuppressWarnings("unchecked")
    default C returnThis() {
        return (C) this;
    }
}


