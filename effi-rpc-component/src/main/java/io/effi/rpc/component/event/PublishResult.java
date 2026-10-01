package io.effi.rpc.component.event;

/**
 * Reports the result of an event publish attempt.
 */
public enum PublishResult {

    /**
     * Indicates that the event was accepted by a lane queue.
     */
    ACCEPTED,

    /**
     * Indicates that the event was dropped by the configured backpressure policy.
     */
    DROPPED,

    /**
     * Indicates that the bus is closed and rejected the event.
     */
    REJECTED
}
