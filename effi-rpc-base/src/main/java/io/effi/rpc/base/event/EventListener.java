package io.effi.rpc.base.event;

/**
 * Listener for handling {@link Event} of type {@link E}.
 */
@FunctionalInterface
public interface EventListener<E extends Event<?>> {

    /**
     * Handles the event when it occurs.
     */
    void onEvent(E event);
}





