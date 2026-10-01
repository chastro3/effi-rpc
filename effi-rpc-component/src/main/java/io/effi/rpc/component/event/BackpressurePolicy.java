package io.effi.rpc.component.event;

/**
 * Defines publisher behavior when an event lane is full.
 */
public enum BackpressurePolicy {

    /**
     * Drops the event and records the loss.
     */
    DROP,

    /**
     * Waits until queue capacity becomes available, the configured publish
     * timeout expires, or the bus closes.
     */
    BLOCK
}
