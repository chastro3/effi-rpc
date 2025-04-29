package io.effi.rpc.event;

import io.effi.rpc.util.Attributes;

/**
 * Represents an event with a source and controls propagation.
 *
 * @param <S> the type of the source
 */
public interface Event<S> extends Attributes {

    /**
     * Returns the source of the event.
     */
    S source();

    /**
     * Sets the source of the event.
     */
    void source(S source);

    /**
     * Stops the event propagation to prevent further notifications.
     */
    boolean stopPropagation();

    /**
     * Checks if event propagation is allowed.
     */
    boolean allowPropagation();
}


