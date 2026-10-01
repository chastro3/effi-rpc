package io.effi.rpc.component.event;

/**
 * Defines event delivery lanes with different reliability and backpressure expectations.
 */
public enum EventLane {

    /**
     * Carries lifecycle and control events that must not be silently discarded.
     */
    CONTROL,

    /**
     * Carries telemetry and observability events that may be dropped under load.
     * Multiple consumers may process this lane concurrently, so global ordering
     * is not guaranteed.
     */
    TELEMETRY
}
