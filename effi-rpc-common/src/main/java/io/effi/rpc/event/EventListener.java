package io.effi.rpc.event;

/**
 * Listener for handling {@link Event} of type {@link E}.
 *
 * @param <E> the type of event this listener handles
 */
@FunctionalInterface
public interface EventListener<E extends Event<?>> {

    /**
     * Handles the event when it occurs.
     */
    void onEvent(E event);
}





