package io.effi.rpc.component.event;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.metrics.CounterSample;
import io.effi.rpc.metrics.MetricsOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MpscEventBusTest {

    private ScopedPlatform platform;

    private MpscEventBus bus;

    private DefaultMetrics metrics;

    @AfterEach
    void closeBus() {
        if (bus != null) {
            bus.close();
        }
        if (metrics != null) {
            metrics.close();
        }
        if (platform != null) {
            platform.close();
        }
    }

    @Test
    void dispatchesToMultipleHandlers() throws Exception {
        bus = newBus();
        CountDownLatch received = new CountDownLatch(2);

        bus.register(PayloadEvent.class, event -> received.countDown());
        bus.register(PayloadEvent.class, event -> received.countDown());

        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("hello")));
        assertTrue(received.await(1, TimeUnit.SECONDS));
    }

    @Test
    void matchesParentEventTypes() throws Exception {
        bus = newBus();
        CountDownLatch received = new CountDownLatch(1);

        bus.register(ParentEvent.class, event -> received.countDown());

        assertEquals(PublishResult.ACCEPTED, bus.publish(new ChildEvent()));
        assertTrue(received.await(1, TimeUnit.SECONDS));
    }

    @Test
    void isolatesHandlerFailure() throws Exception {
        bus = newBus();
        CountDownLatch received = new CountDownLatch(1);

        bus.register(PayloadEvent.class, event -> {
            throw new IllegalStateException("boom");
        });
        bus.register(PayloadEvent.class, event -> received.countDown());

        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("hello")));
        assertTrue(received.await(1, TimeUnit.SECONDS));
        assertEquals(1L, metrics.counter(EventBusMetrics.HANDLED).count());
        assertEquals(1L, metrics.counter(EventBusMetrics.FAILED).count());
    }

    @Test
    void dropsWhenFull() throws Exception {
        bus = newBus(1, 1, 10_000L, true);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        bus.register(GateEvent.class, event -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });

        assertEquals(PublishResult.ACCEPTED, bus.publish(new GateEvent()));
        assertTrue(entered.await(1, TimeUnit.SECONDS));
        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("queued")));
        assertEquals(PublishResult.DROPPED, bus.publish(new PayloadEvent("dropped")));
        assertEquals(1L, metrics.counter(EventBusMetrics.DROPPED).count());

        release.countDown();
    }

    @Test
    void blocksPublisherWhenConfigured() throws Exception {
        bus = newBus(1, 1, 10_000L, true);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        bus.register(GateEvent.class, event -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });

        assertEquals(PublishResult.ACCEPTED, bus.publish(new GateEvent()));
        assertTrue(entered.await(1, TimeUnit.SECONDS));
        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("queued")));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<PublishResult> blocked = executor.submit(() ->
                    bus.publish(new PayloadEvent("blocked"), BackpressurePolicy.BLOCK));
            assertFalse(blocked.isDone());
            release.countDown();
            assertEquals(PublishResult.ACCEPTED, blocked.get(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void blocksControlEventsByDefaultWhenFull() throws Exception {
        bus = newBus(1, 1, 10_000L, true);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        bus.register(ControlGateEvent.class, event -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });

        assertEquals(PublishResult.ACCEPTED, bus.publish(new ControlGateEvent()));
        assertTrue(entered.await(1, TimeUnit.SECONDS));
        assertEquals(PublishResult.ACCEPTED, bus.publish(new ControlGateEvent()));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<PublishResult> blocked = executor.submit(() -> bus.publish(new ControlGateEvent()));
            assertFalse(blocked.isDone());
            release.countDown();
            assertEquals(PublishResult.ACCEPTED, blocked.get(1, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rejectsAfterClose() {
        bus = newBus();
        bus.close();

        assertEquals(PublishResult.REJECTED, bus.publish(new PayloadEvent("closed")));
        assertEquals(1L, metrics.counter(EventBusMetrics.REJECTED).count());
    }

    @Test
    void drainsQueuedEventsOnClose() throws Exception {
        bus = newBus(64, 4, 10_000L, true);
        int eventCount = 32;
        CountDownLatch received = new CountDownLatch(eventCount);
        AtomicInteger handled = new AtomicInteger();

        bus.register(PayloadEvent.class, event -> {
            handled.incrementAndGet();
            received.countDown();
        });

        for (int i = 0; i < eventCount; i++) {
            assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("event-" + i)));
        }

        bus.close();

        assertEquals(eventCount, handled.get());
        assertTrue(received.await(1, TimeUnit.SECONDS));
    }

    @Test
    void dispatchesAcrossTelemetryShards() throws Exception {
        int eventCount = 1_000;
        bus = newBus(1_024, 256, 10_000L, true, false, 4);
        Set<String> handled = ConcurrentHashMap.newKeySet();
        CountDownLatch received = new CountDownLatch(eventCount);

        bus.register(PayloadEvent.class, event -> {
            if (handled.add(event.value())) {
                received.countDown();
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<PublishResult>> results = new ArrayList<>(eventCount);
            for (int i = 0; i < eventCount; i++) {
                String value = "event-" + i;
                results.add(executor.submit(() ->
                        bus.publish(new PayloadEvent(value), BackpressurePolicy.BLOCK)));
            }
            for (Future<PublishResult> result : results) {
                assertEquals(PublishResult.ACCEPTED, result.get(1, TimeUnit.SECONDS));
            }
            assertTrue(received.await(1, TimeUnit.SECONDS));
            assertEquals(eventCount, handled.size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void doesNotStartConsumersUntilStarted() {
        platform = new ScopedPlatform("event-bus-start-test-" + System.nanoTime());
        metrics = new DefaultMetrics(platform);
        bus = new MpscEventBus(platform);
        metrics.register(bus.metrics());

        assertFalse(bus.active());
        bus.start();
        assertTrue(bus.active());
    }

    @Test
    void timesOutBlockedPublisher() throws Exception {
        bus = newBus(1, 1, 10_000L, true, true, 1, 20_000_000L);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        bus.register(GateEvent.class, event -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });

        try {
            assertEquals(PublishResult.ACCEPTED, bus.publish(new GateEvent()));
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("queued")));

            long startedAt = System.nanoTime();
            assertEquals(PublishResult.REJECTED,
                    bus.publish(new PayloadEvent("blocked"), BackpressurePolicy.BLOCK));
            assertTrue(System.nanoTime() - startedAt >= 15_000_000L);
        } finally {
            release.countDown();
        }
    }

    @Test
    void closeRejectsBlockedPublisherBeforeJoiningConsumers() throws Exception {
        bus = newBus(1, 1, 10_000L, true);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        bus.register(GateEvent.class, event -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });

        assertEquals(PublishResult.ACCEPTED, bus.publish(new GateEvent()));
        assertTrue(entered.await(1, TimeUnit.SECONDS));
        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("queued")));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<PublishResult> blocked = executor.submit(() ->
                    bus.publish(new PayloadEvent("blocked"), BackpressurePolicy.BLOCK));
            Thread.sleep(20);
            assertFalse(blocked.isDone());

            Future<?> closing = executor.submit(bus::close);
            assertEquals(PublishResult.REJECTED, blocked.get(1, TimeUnit.SECONDS));
            release.countDown();
            closing.get(1, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void failsBusWhenHandlerThrowsError() throws Exception {
        bus = newBus();
        CountDownLatch entered = new CountDownLatch(1);
        bus.register(PayloadEvent.class, event -> {
            entered.countDown();
            throw new AssertionError("boom");
        });

        assertEquals(PublishResult.ACCEPTED, bus.publish(new PayloadEvent("fatal")));
        assertTrue(entered.await(1, TimeUnit.SECONDS));
        for (int i = 0; i < 100 && bus.active(); i++) {
            Thread.sleep(10);
        }

        assertFalse(bus.active());
        assertEquals(PublishResult.REJECTED, bus.publish(new PayloadEvent("after-failure")));
        assertEquals(1L, metrics.counter(EventBusMetrics.FAILED).count());
        assertEquals(1L, metrics.counter(EventBusMetrics.CONSUMER_FAILED).count());
    }

    @Test
    void rejectsTooManyTelemetryConsumers() {
        assertThrows(IllegalArgumentException.class,
                () -> newBus(16, 4, 10_000L, true, true, 1_025));
    }

    @Test
    void reportsMetricsDisabled() {
        bus = newBus(16, 4, 10_000L, true, false, 1);
        bus.publish(new PayloadEvent("ignored"));

        assertTrue(metrics.snapshot().samples().isEmpty());
        assertEquals(0L, metrics.counter(EventBusMetrics.PUBLISHED).count());
    }

    @Test
    void exposesUnifiedMetricSamples() throws Exception {
        bus = newBus();
        CountDownLatch received = new CountDownLatch(1);
        bus.register(PayloadEvent.class, event -> received.countDown());

        bus.publish(new PayloadEvent("metrics"));
        assertTrue(received.await(1, TimeUnit.SECONDS));

        CounterSample accepted = metrics.snapshot().samples().stream()
                .filter(sample -> sample.key().equals(EventBusMetrics.ACCEPTED))
                .map(CounterSample.class::cast)
                .findFirst()
                .orElseThrow();
        assertEquals(1L, accepted.value());
    }

    private MpscEventBus newBus() {
        return newBus(16, 4, 10_000L, true, true, 1);
    }

    private MpscEventBus newBus(int capacity, int batchSize, long idleParkNanos, boolean daemon) {
        return newBus(capacity, batchSize, idleParkNanos, daemon, true, 1);
    }

    private MpscEventBus newBus(
            int capacity,
            int batchSize,
            long idleParkNanos,
            boolean daemon,
            boolean metricsEnabled,
            int telemetryConsumers
    ) {
        return newBus(capacity, batchSize, idleParkNanos, daemon, metricsEnabled,
                telemetryConsumers, EventOptions.PUBLISH_TIMEOUT_NANOS.defaultValue());
    }

    private MpscEventBus newBus(
            int capacity,
            int batchSize,
            long idleParkNanos,
            boolean daemon,
            boolean metricsEnabled,
            int telemetryConsumers,
            long publishTimeoutNanos
    ) {
        platform = new ScopedPlatform("event-bus-test-" + System.nanoTime());
        platform.options()
                .addOption(EventOptions.CAPACITY, capacity)
                .addOption(EventOptions.BATCH_SIZE, batchSize)
                .addOption(EventOptions.IDLE_PARK_NANOS, idleParkNanos)
                .addOption(EventOptions.PUBLISH_TIMEOUT_NANOS, publishTimeoutNanos)
                .addOption(EventOptions.DAEMON, daemon)
                .addOption(MetricsOptions.ENABLED, metricsEnabled)
                .addOption(EventOptions.TELEMETRY_CONSUMERS, telemetryConsumers);
        metrics = new DefaultMetrics(platform);
        MpscEventBus eventBus = new MpscEventBus(platform);
        metrics.register(eventBus.metrics());
        eventBus.start();
        return eventBus;
    }

    private static final class PayloadEvent implements Event {

        private final String value;

        private PayloadEvent(String value) {
            this.value = value;
        }

        private String value() {
            return value;
        }
    }

    private static class ParentEvent implements Event {
    }

    private static final class ChildEvent extends ParentEvent {
    }

    private static final class GateEvent implements Event {
    }

    private static final class ControlGateEvent implements Event {

        @Override
        public EventLane lane() {
            return EventLane.CONTROL;
        }
    }
}
