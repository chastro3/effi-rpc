package io.effi.rpc.metrics;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsTest {

    @Test
    void aggregatesInstrumentsAndReporters() {
        DefaultMetrics metrics = new DefaultMetrics(ScopedPlatform.defaultInstance());
        MetricKey count = MetricKey.of("test.count");
        MetricKey duration = MetricKey.of("test.duration");
        MetricKey gauge = MetricKey.of("test.gauge");

        metrics.counter(count).add(3L);
        metrics.timer(duration).recordNanos(10L);
        metrics.timer(duration).recordNanos(30L);
        metrics.gauge(gauge, () -> 7D);

        AtomicReference<MetricsSnapshot> reported = new AtomicReference<>();
        metrics.registerReporter(reported::set);
        metrics.report();

        MetricsSnapshot snapshot = metrics.snapshot();
        assertEquals(3, snapshot.samples().size());
        assertTrue(snapshot.samples().stream().anyMatch(sample -> sample.key().equals(count)));
        assertEquals(3, reported.get().samples().size());
    }

    @Test
    void timerSnapshotKeepsNanosecondAggregate() {
        DefaultMetrics metrics = new DefaultMetrics(ScopedPlatform.defaultInstance());
        MetricTimer timer = metrics.timer(MetricKey.of("test.timer"));

        timer.recordNanos(100L);
        timer.recordNanos(300L);

        TimerSample snapshot = timer.snapshot();
        assertEquals(2L, snapshot.count());
        assertEquals(400L, snapshot.totalNanos());
        assertEquals(300L, snapshot.maxNanos());
        assertEquals(100L, snapshot.minNanos());
        assertEquals(MetricUnit.NANOSECONDS, snapshot.unit());
    }
}
