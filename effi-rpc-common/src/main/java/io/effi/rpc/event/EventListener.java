package io.effi.rpc.event;

/**
 * Listener for handling events of type {@link E}.
 *
 * @param <E> the type of the event this listener handles, which extends {@link Event}
 */
@FunctionalInterface
public interface EventListener<E extends Event<?>> {

    /**
     * Handles the event when it occurs.
     *
     * @param event the event that has occurred
     */
    void onEvent(E event);
}




