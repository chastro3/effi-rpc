package io.effi.rpc.component.event;

/**
 * Captures a point-in-time event bus metrics snapshot.
 */
public record EventBusMetrics(
        /** Whether counters are enabled. */
        boolean enabled,
        /** Total publish attempts. */
        long published,
        /** Events accepted into lane queues. */
        long accepted,
        /** Events dropped by backpressure policy. */
        long dropped,
        /** Events rejected because the bus is closed. */
        long rejected,
        /** Events dispatched to at least one handler. */
        long handled,
        /** Handler failures. */
        long failed,
        /** Events currently waiting in lane queues. */
        long pending
) {
}
