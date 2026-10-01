package io.effi.rpc.component.event;

import java.util.concurrent.atomic.LongAdder;

/**
 * Records event bus counters and creates point-in-time snapshots.
 */
final class EventBusCounters {

    private final boolean enabled;

    private final LongAdder published = new LongAdder();

    private final LongAdder accepted = new LongAdder();

    private final LongAdder dropped = new LongAdder();

    private final LongAdder rejected = new LongAdder();

    private final LongAdder handled = new LongAdder();

    private final LongAdder failed = new LongAdder();

    EventBusCounters(boolean enabled) {
        this.enabled = enabled;
    }

    void published() {
        if (enabled) published.increment();
    }

    void accepted() {
        if (enabled) accepted.increment();
    }

    void dropped() {
        if (enabled) dropped.increment();
    }

    void rejected() {
        if (enabled) rejected.increment();
    }

    void handled() {
        if (enabled) handled.increment();
    }

    void failed() {
        if (enabled) failed.increment();
    }

    EventBusMetrics snapshot(long pending) {
        return new EventBusMetrics(
                enabled,
                published.sum(),
                accepted.sum(),
                dropped.sum(),
                rejected.sum(),
                handled.sum(),
                failed.sum(),
                pending
        );
    }
}
