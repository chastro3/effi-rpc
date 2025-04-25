package io.effi.rpc.event;

import io.effi.rpc.util.Attributes;

/**
 * Represents an event with a source and supports propagation control.
 *
 * @param <S> the type of the source
 */
public interface Event<S> extends Attributes {

    /**
     * Gets the source of the event.
     *
     * @return the event source
     */
    S source();

    /**
     * Sets the source of the event.
     *
     * @param source the source to set
     */
    void source(S source);

    /**
     * Stops the event propagation, preventing further listeners from being notified.
     *
     * @return true if propagation is stopped, false otherwise
     */
    boolean stopPropagation();

    /**
     * Checks if the event propagation is allowed.
     *
     * @return true if propagation is allowed, false otherwise
     */
    boolean allowPropagation();
}

