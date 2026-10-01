package io.effi.rpc.component.event;

/**
 * Handles events of a single compatible type.
 * <p>
 * Handlers registered for telemetry events may be invoked concurrently by
 * multiple consumer lanes and must be thread-safe.
 */
@FunctionalInterface
public interface EventHandler<E extends Event> {

    /**
     * Handles the supplied event.
     *
     * @param event event to handle
     */
    void onEvent(E event);

}
