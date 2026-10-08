package io.effi.rpc.component.event;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Provides a bounded MPSC event bus with isolated control and telemetry consumers.
 * <p>
 * Handlers must be registered before {@link #start()} begins consuming events.
 */
public final class MpscEventBus extends ScopedPlatform.Holder implements EventBus {

    private static final Logger logger = LoggerFactory.getLogger(MpscEventBus.class);

    private static final long CLOSE_JOIN_MILLIS = 5_000L;

    private static final long BLOCK_PARK_NANOS = 1_000L;

    private static final int MAX_TELEMETRY_CONSUMERS = 1_024;

    private final long publishTimeoutNanos;

    private final HandlerRegistry registry = new HandlerRegistry();

    private final EventBusMetrics metrics = new EventBusMetrics(this);

    private final PublisherGate publisherGate = new PublisherGate();

    private final EventConsumerLane controlLane;

    private final EventConsumerLane[] telemetryLanes;

    private final int telemetryLaneMask;

    private final AtomicBoolean running = new AtomicBoolean();

    public MpscEventBus(ScopedPlatform platform) {
        super(platform);
        int batchSize = requirePositive(
                platform.options().option(EventOptions.BATCH_SIZE),
                EventOptions.BATCH_SIZE.name()
        );
        long idleParkNanos = requireNonNegative(
                platform.options().option(EventOptions.IDLE_PARK_NANOS),
                EventOptions.IDLE_PARK_NANOS.name()
        );
        this.publishTimeoutNanos = requireNonNegative(
                platform.options().option(EventOptions.PUBLISH_TIMEOUT_NANOS),
                EventOptions.PUBLISH_TIMEOUT_NANOS.name()
        );
        boolean daemon = platform.options().option(EventOptions.DAEMON);
        int capacity = requirePositive(
                platform.options().option(EventOptions.CAPACITY),
                EventOptions.CAPACITY.name()
        );
        int telemetryConsumers = requireRange(
                platform.options().option(EventOptions.TELEMETRY_CONSUMERS),
                EventOptions.TELEMETRY_CONSUMERS.name()
        );
        this.controlLane = new EventConsumerLane(
                EventLane.CONTROL, 0, capacity, daemon,
                registry, metrics::handled, metrics::failed, batchSize, idleParkNanos,
                running::get, this::fail
        );
        int telemetryLaneCount = powerOfTwo(telemetryConsumers);
        this.telemetryLanes = new EventConsumerLane[telemetryLaneCount];
        this.telemetryLaneMask = telemetryLaneCount - 1;
        for (int i = 0; i < telemetryLaneCount; i++) {
            telemetryLanes[i] = new EventConsumerLane(
                    EventLane.TELEMETRY, i, capacity, daemon,
                    registry, metrics::handled, metrics::failed, batchSize, idleParkNanos,
                    running::get, this::fail
            );
        }
    }

    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0");
        }
        return value;
    }

    private static long requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be >= 0");
        }
        return value;
    }

    private static int requireRange(int value, String name) {
        if (value < 1 || value > MpscEventBus.MAX_TELEMETRY_CONSUMERS) {
            throw new IllegalArgumentException(name + " must be between " + 1 + " and " + MpscEventBus.MAX_TELEMETRY_CONSUMERS);
        }
        return value;
    }

    private void fail(EventConsumerLane lane, Throwable failure) {
        if (!publisherGate.fail(failure)) {
            return;
        }
        running.set(false);
        metrics.consumerFailed();
        logger.error("Event consumer '{}' failed", failure, lane.name());
        controlLane.unpark();
        for (EventConsumerLane telemetryLane : telemetryLanes) {
            telemetryLane.unpark();
        }
    }

    private static int powerOfTwo(int value) {
        int result = 1;
        while (result < value) {
            result <<= 1;
        }
        return result;
    }

    /**
     * Starts event consumers after handlers have been registered.
     */
    public void start() {
        if (publisherGate.closed()) {
            throw new IllegalStateException("Event bus is closed");
        }
        if (publisherGate.failed()) {
            throw new IllegalStateException("Event bus failed", publisherGate.failure());
        }
        if (running.compareAndSet(false, true)) {
            publisherGate.start();
            controlLane.start();
            for (EventConsumerLane lane : telemetryLanes) {
                lane.start();
            }
        }
    }

    @Override
    public <E extends Event> EventBus register(Class<E> eventType, EventHandler<E> handler) {
        registry.register(eventType, handler);
        return this;
    }

    @Override
    public PublishResult publish(Event event, BackpressurePolicy policy) {
        AssertUtil.notNull(event, "event");
        AssertUtil.notNull(policy, "policy");
        if (policy == BackpressurePolicy.BLOCK) {
            if (!publisherGate.begin()) {
                metrics.rejected();
                return PublishResult.REJECTED;
            }
            try {
                return doPublish(event, policy);
            } finally {
                publisherGate.end();
            }
        }
        if (!publisherGate.canPublish()) {
            metrics.rejected();
            return PublishResult.REJECTED;
        }
        return doPublish(event, policy);
    }

    private PublishResult doPublish(Event event, BackpressurePolicy policy) {
        metrics.published();
        EventConsumerLane lane = lane(event);
        if (lane.offer(event)) {
            metrics.accepted();
            return PublishResult.ACCEPTED;
        }
        return switch (policy) {
            case DROP -> {
                metrics.dropped();
                yield PublishResult.DROPPED;
            }
            case BLOCK -> blockUntilAccepted(lane, event);
        };
    }

    private EventConsumerLane lane(Event event) {
        EventLane lane = event.lane();
        if (lane == EventLane.CONTROL) {
            return controlLane;
        }
        if (telemetryLanes.length == 1) {
            return telemetryLanes[0];
        }
        // Hash the thread id so adjacent ids do not collapse onto the same lane.
        long threadId = Thread.currentThread().threadId();
        int index = (int) ((threadId * 0x9E3779B97F4A7C15L) >>> 32) & telemetryLaneMask;
        return telemetryLanes[index];
    }

    private PublishResult blockUntilAccepted(EventConsumerLane lane, Event event) {
        long deadline = System.nanoTime() + publishTimeoutNanos;
        boolean interrupted = false;
        while (publisherGate.canPublish()) {
            if (lane.offer(event)) {
                metrics.accepted();
                return PublishResult.ACCEPTED;
            }
            if (Thread.interrupted()) {
                interrupted = true;
                break;
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            LockSupport.parkNanos(Math.min(BLOCK_PARK_NANOS, remaining));
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        metrics.rejected();
        return PublishResult.REJECTED;
    }

    /**
     * Returns the metrics registrar owned by this event bus.
     *
     * @return event bus metrics registrar
     */
    public EventBusMetrics metrics() {
        return metrics;
    }

    @Override
    public void close() {
        if (!publisherGate.close()) {
            return;
        }
        running.set(false);
        controlLane.unpark();
        for (EventConsumerLane lane : telemetryLanes) {
            lane.unpark();
        }
        controlLane.join(CLOSE_JOIN_MILLIS);
        for (EventConsumerLane lane : telemetryLanes) {
            lane.join(CLOSE_JOIN_MILLIS);
        }
    }

    @Override
    public boolean active() {
        return running.get() && !publisherGate.closed() && !publisherGate.failed();
    }

    public long pendingCount() {
        long pending = controlLane.size();
        for (EventConsumerLane lane : telemetryLanes) {
            pending += lane.size();
        }
        return pending;
    }

}
