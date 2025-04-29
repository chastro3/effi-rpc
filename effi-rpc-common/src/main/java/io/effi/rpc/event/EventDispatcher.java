package io.effi.rpc.event;

import io.effi.rpc.util.resoruce.Closeable;

/**
 * Dispatches events and manages event listeners.
 */
public interface EventDispatcher extends Closeable {

    /**
     * Registers a listener for the specified event type.
     */
    <E extends Event<?>> void registerListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Removes a listener for the specified event type.
     */
    <E extends Event<?>> void removeListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Dispatches the event to all registered listeners.
     */
    <E extends Event<?>> void publish(E event);
}


