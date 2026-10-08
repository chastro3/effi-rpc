package io.effi.rpc.component.event;

import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;

/**
 * Registers and records EventBus metrics.
 */
public final class EventBusMetrics implements MetricsRegistrar {

    public static final MetricKey PUBLISHED = publish("published");

    public static final MetricKey ACCEPTED = publish("accepted");

    public static final MetricKey DROPPED = publish("dropped");

    public static final MetricKey REJECTED = publish("rejected");

    public static final MetricKey HANDLED = event("handled");

    public static final MetricKey FAILED = event("failed");

    public static final MetricKey PENDING = MetricKey.of("eventbus.queue.pending");

    public static final MetricKey CONSUMER_FAILED = MetricKey.of("eventbus.consumer.failure.count");

    private final MpscEventBus bus;

    private MetricCounter published = MetricCounter.NOOP;

    private MetricCounter accepted = MetricCounter.NOOP;

    private MetricCounter dropped = MetricCounter.NOOP;

    private MetricCounter rejected = MetricCounter.NOOP;

    private MetricCounter handled = MetricCounter.NOOP;

    private MetricCounter failed = MetricCounter.NOOP;

    private MetricCounter consumerFailed = MetricCounter.NOOP;

    public EventBusMetrics(MpscEventBus bus) {
        this.bus = bus;
    }

    private static MetricKey publish(String result) {
        return MetricKey.of("eventbus.publish.count").withTag("result", result);
    }

    private static MetricKey event(String result) {
        return MetricKey.of("eventbus.event.count").withTag("result", result);
    }

    @Override
    public void register(Metrics metrics) {
        this.published = metrics.counter(PUBLISHED);
        this.accepted = metrics.counter(ACCEPTED);
        this.dropped = metrics.counter(DROPPED);
        this.rejected = metrics.counter(REJECTED);
        this.handled = metrics.counter(HANDLED);
        this.failed = metrics.counter(FAILED);
        this.consumerFailed = metrics.counter(CONSUMER_FAILED);
        metrics.gauge(PENDING, bus::pendingCount);
    }

    public void published() {
        published.increment();
    }

    public void accepted() {
        accepted.increment();
    }

    public void dropped() {
        dropped.increment();
    }

    public void rejected() {
        rejected.increment();
    }

    public void handled() {
        handled.increment();
    }

    public void failed() {
        failed.increment();
    }

    public void consumerFailed() {
        consumerFailed.increment();
    }
}
