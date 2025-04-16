package io.effi.rpc.common.event;

import io.effi.rpc.common.util.resoruce.Closeable;

/**
 * Dispatches events and manages event listeners.
 */
public interface EventDispatcher extends Closeable {

    /**
     * Registers an event listener for the specified event type.
     *
     * @param eventType the class object representing the event type
     * @param listener  the event listener to register
     * @param <E>       the type of the event
     */
    <E extends Event<?>> void registerListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Removes an event listener for the specified event type.
     *
     * @param eventType the class object representing the event type
     * @param listener  the event listener to remove
     * @param <E>       the type of the event
     */
    <E extends Event<?>> void removeListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Dispatches the event to all registered listeners for its type.
     *
     * @param event the event to dispatch
     * @param <E>   the type of the event
     */
    <E extends Event<?>> void publish(E event);

}

