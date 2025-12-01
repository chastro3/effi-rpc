package io.effi.rpc.context.metrics;

import java.util.Map;

public class TimerMetricEvent extends MetricEvent {

    private final long durationNanos;

    private final boolean success;

    public TimerMetricEvent(String name, Map<String, String> tags, long durationNanos, boolean success) {
        super(name, tags);
        this.durationNanos = durationNanos;
        this.success = success;
    }

    public long durationNanos() {
        return durationNanos;
    }

    public boolean succeeded() {
        return success;
    }
}
