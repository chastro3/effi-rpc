package io.effi.rpc.component.event;

/**
 * Listener for handling {@link Event} of type {@link E}.
 */
@FunctionalInterface
public interface EventListener<E extends Event<?>> extends java.util.EventListener {

    /**
     * Handles the event when it occurs.
     */
    void onEvent(E event);
}





