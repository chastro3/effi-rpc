package io.effi.rpc.component.metrics;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.metrics.CounterSample;
import io.effi.rpc.metrics.GaugeSample;
import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricGauge;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricSample;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsOptions;
import io.effi.rpc.metrics.MetricsRegistrar;
import io.effi.rpc.metrics.MetricsReporter;
import io.effi.rpc.metrics.MetricsSnapshot;
import io.effi.rpc.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleSupplier;

/**
 * Default platform metrics registry.
 */
public final class DefaultMetrics implements Metrics {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMetrics.class);

    private final boolean enabled;

    private final ConcurrentMap<MetricKey, MetricCounter> counters = new ConcurrentHashMap<>();

    private final ConcurrentMap<MetricKey, MetricTimer> timers = new ConcurrentHashMap<>();

    private final ConcurrentMap<MetricKey, MetricGauge> gauges = new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<MetricsReporter> reporters = new CopyOnWriteArrayList<>();

    private final AtomicBoolean active = new AtomicBoolean(true);

    public DefaultMetrics(ScopedPlatform platform) {
        AssertUtil.notNull(platform, "platform");
        this.enabled = platform.options().option(MetricsOptions.ENABLED);
    }

    @Override
    public MetricCounter counter(MetricKey key) {
        AssertUtil.notNull(key, "key");
        if (!enabled) {
            return MetricCounter.NOOP;
        }
        return counters.computeIfAbsent(key, ignored -> new DefaultMetricCounter());
    }

    @Override
    public MetricTimer timer(MetricKey key) {
        AssertUtil.notNull(key, "key");
        if (!enabled) {
            return MetricTimer.NOOP;
        }
        return timers.computeIfAbsent(key, DefaultMetricTimer::new);
    }

    @Override
    public MetricGauge gauge(MetricKey key, DoubleSupplier supplier) {
        AssertUtil.notNull(key, "key");
        AssertUtil.notNull(supplier, "supplier");
        if (!enabled) {
            return MetricGauge.NOOP;
        }
        return gauges.computeIfAbsent(key, ignored -> new DefaultMetricGauge(supplier));
    }

    @Override
    public void register(MetricsRegistrar registrar) {
        AssertUtil.notNull(registrar, "registrar");
        registrar.register(this);
    }

    @Override
    public void registerReporter(MetricsReporter reporter) {
        AssertUtil.notNull(reporter, "reporter");
        reporters.addIfAbsent(reporter);
    }

    @Override
    public MetricsSnapshot snapshot() {
        long timestamp = System.nanoTime();
        if (!enabled) {
            return new MetricsSnapshot(timestamp, List.of());
        }
        List<MetricSample> samples = new ArrayList<>(counters.size() + timers.size() + gauges.size());
        counters.forEach((key, counter) -> samples.add(new CounterSample(key, counter.count(), timestamp)));
        gauges.forEach((key, gauge) -> samples.add(new GaugeSample(key, gauge.value(), timestamp)));
        timers.forEach((key, timer) -> samples.add(timer.snapshot()));
        return new MetricsSnapshot(timestamp, samples);
    }

    @Override
    public void report() {
        if (reporters.isEmpty()) {
            return;
        }
        MetricsSnapshot snapshot = snapshot();
        for (MetricsReporter reporter : reporters) {
            try {
                reporter.report(snapshot);
            } catch (Throwable e) {
                logger.warn("Metrics reporter '{}' failed", e, reporter.getClass().getName());
            }
        }
    }

    @Override
    public void close() {
        if (!active.compareAndSet(true, false)) {
            return;
        }
        report();
        counters.clear();
        timers.clear();
        gauges.clear();
        reporters.clear();
    }

    @Override
    public boolean active() {
        return active.get();
    }
}
