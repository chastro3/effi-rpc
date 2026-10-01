package io.effi.rpc.component.event;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.trait.Closeable;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Dispatches framework events through independent control and telemetry lanes.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface EventBus extends Closeable {

    /**
     * Registers a handler for the supplied event type or one of its subtypes.
     *
     * @param eventType event type
     * @param handler event handler
     * @param <E> event type
     * @return this event bus
     */
    <E extends Event> EventBus register(Class<E> eventType, EventHandler<E> handler);

    /**
     * Publishes an event using the supplied backpressure policy.
     *
     * @param event event to publish
     * @param policy full-queue policy
     * @return publish result
     */
    PublishResult publish(Event event, BackpressurePolicy policy);

    /**
     * Publishes an event using its declared backpressure policy.
     *
     * @param event event to publish
     * @return publish result
     */
    default PublishResult publish(Event event) {
        return publish(event, event.backpressurePolicy());
    }

    /**
     * Returns the current metrics snapshot.
     */
    EventBusMetrics metrics();
}
