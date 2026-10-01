package io.effi.rpc.component.event;

/**
 * Defines an event handled by the RPC event bus.
 */
public interface Event {

    /**
     * Returns the delivery lane for this event.
     */
    default EventLane lane() {
        return EventLane.TELEMETRY;
    }

    /**
     * Returns the backpressure policy applied when the target lane is full.
     * <p>
     * Control events block by default because dropping lifecycle events can
     * leave resources in an inconsistent state. Telemetry events drop by
     * default because stale observability data must not stall RPC traffic.
     */
    default BackpressurePolicy backpressurePolicy() {
        return lane() == EventLane.CONTROL
                ? BackpressurePolicy.BLOCK
                : BackpressurePolicy.DROP;
    }
}
