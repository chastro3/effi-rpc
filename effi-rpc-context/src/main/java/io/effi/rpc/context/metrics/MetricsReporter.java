package io.effi.rpc.context.metrics;

import java.util.List;

public interface MetricsReporter {

    void report(List<MetricEvent> events);
}
