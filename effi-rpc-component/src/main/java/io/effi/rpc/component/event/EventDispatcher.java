package io.effi.rpc.component.event;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.util.resoruce.Closeable;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Dispatches events and manages event listeners.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface EventDispatcher extends Closeable, ScopedPlatform.Supplier {

    /**
     * Registers a listener for the specified event type.
     */
    <E extends Event<?>>

    void registerListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Removes a listener for the specified event type.
     */
    <E extends Event<?>> void removeListener(Class<E> eventType, EventListener<E> listener);

    /**
     * Dispatches the event to all registered listeners.
     */
    <E extends Event<?>> void publish(E event);
}


