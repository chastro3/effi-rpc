package io.effi.rpc.context.metrics;

import java.util.Map;

public class CounterMetricEvent extends MetricEvent {
    private final long count;

    public CounterMetricEvent(String name, Map<String,String> tags, long count) {
        super(name, tags);
        this.count = count;
    }

    public long count() { return count; }
}
