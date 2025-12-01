package io.effi.rpc.context.metrics;

import java.util.Map;

public abstract class MetricEvent {

    private final String name;

    private final Map<String, String> tags;

    public MetricEvent(String name, Map<String, String> tags) {
        this.name = name;
        this.tags = tags;
    }

    public String name() {
        return name;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
