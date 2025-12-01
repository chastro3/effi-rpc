package io.effi.rpc.context.metrics;

public interface MetricsCollector {

    void record(MetricEvent... event);

    void registerReporter(MetricsReporter reporter);
}
