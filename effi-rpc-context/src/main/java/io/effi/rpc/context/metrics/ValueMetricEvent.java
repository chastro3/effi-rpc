package io.effi.rpc.context.metrics;

import java.util.Map;

public class ValueMetricEvent extends MetricEvent {
    private final double value;

    public ValueMetricEvent(String name, Map<String,String> tags, double value) {
        super(name, tags);
        this.value = value;
    }

    public double value() { return value; }
}
