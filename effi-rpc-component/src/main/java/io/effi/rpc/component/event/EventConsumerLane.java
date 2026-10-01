package io.effi.rpc.component.event;

import io.effi.rpc.executor.ConfigurableThreadFactory;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.util.ObjectUtil;
import org.jctools.queues.MpscArrayQueue;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.concurrent.locks.LockSupport;

/**
 * Runs the consumer loop for one control or telemetry lane.
 */
final class EventConsumerLane {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumerLane.class);

    private final MpscArrayQueue<Event> queue;

    private final Thread thread;

    private final HandlerRegistry registry;

    private final HandlerRegistry.Cache handlerCache = new HandlerRegistry.Cache();

    private final Runnable handled;

    private final Runnable failed;

    private final int batchSize;

    private final long idleParkNanos;

    private final BooleanSupplier running;

    private final BiConsumer<EventConsumerLane, Throwable> failureHandler;

    EventConsumerLane(
            EventLane lane,
            int index,
            int capacity,
            boolean daemon,
            HandlerRegistry registry,
            Runnable handled,
            Runnable failed,
            int batchSize,
            long idleParkNanos,
            BooleanSupplier running,
            BiConsumer<EventConsumerLane, Throwable> failureHandler
    ) {
        this.queue = new MpscArrayQueue<>(capacity);
        this.registry = registry;
        this.handled = handled;
        this.failed = failed;
        this.batchSize = batchSize;
        this.idleParkNanos = idleParkNanos;
        this.running = running;
        this.failureHandler = failureHandler;
        this.thread = new ConfigurableThreadFactory()
                .namePrefix("rpc-event-" + lane.name().toLowerCase() + "-" + index)
                .daemon(daemon)
                .newThread(this::consume);
    }

    boolean offer(Event event) {
        return queue.offer(event);
    }

    long size() {
        return queue.size();
    }

    void start() {
        thread.start();
    }

    void unpark() {
        LockSupport.unpark(thread);
    }

    void join(long millis) {
        try {
            thread.join(millis);
            if (thread.isAlive()) {
                logger.warn("Event consumer '{}' did not stop within {} ms", thread.getName(), millis);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    String name() {
        return thread.getName();
    }

    private void consume() {
        Event[] batch = new Event[batchSize];
        try {
            while (running.getAsBoolean() || !queue.isEmpty()) {
                int count = drain(batch);
                if (count == 0) {
                    if (!running.getAsBoolean()) {
                        break;
                    }
                    LockSupport.parkNanos(idleParkNanos);
                    continue;
                }
                for (int i = 0; i < count; i++) {
                    dispatch(batch[i]);
                    batch[i] = null;
                }
            }
        } catch (Throwable failure) {
            failureHandler.accept(this, failure);
        }
    }

    private int drain(Event[] batch) {
        int count = 0;
        while (count < batch.length) {
            Event event = queue.poll();
            if (event == null) {
                break;
            }
            batch[count++] = event;
        }
        return count;
    }

    private void dispatch(Event event) {
        HandlerRegistry.Chain chain = registry.resolve(event.getClass(), handlerCache);
        EventHandler<?>[] handlers = chain.handlers();
        if (handlers.length == 0) {
            return;
        }
        handled.run();
        boolean eventFailed = false;
        for (EventHandler<?> handler : handlers) {
            try {
                invoke(handler, event);
            } catch (Throwable failure) {
                if (!eventFailed) {
                    eventFailed = true;
                    failed.run();
                }
                if (failure instanceof Error) {
                    throw failure;
                }
                logger.error("Failed to handle '{}' event in '{}'",
                        failure,
                        ObjectUtil.simpleClassName(event),
                        ObjectUtil.simpleClassName(handler));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void invoke(EventHandler<?> handler, Event event) {
        ((EventHandler<Event>) handler).onEvent(event);
    }
}
