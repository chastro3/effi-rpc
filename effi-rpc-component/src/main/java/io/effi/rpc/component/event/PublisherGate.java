package io.effi.rpc.component.event;

import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Coordinates publisher admission and shutdown draining.
 */
final class PublisherGate {

    private static final Logger logger = LoggerFactory.getLogger(PublisherGate.class);

    private static final long CLOSE_WAIT_MILLIS = 1_000L;

    private static final long WAIT_PARK_NANOS = 1_000L;

    private final AtomicBoolean accepting = new AtomicBoolean();

    private final AtomicBoolean closed = new AtomicBoolean();

    private final AtomicBoolean failed = new AtomicBoolean();

    private final AtomicInteger publisherCount = new AtomicInteger();

    private volatile Throwable failure;

    void start() {
        if (closed.get()) {
            throw new IllegalStateException("Event bus is closed");
        }
        if (failed.get()) {
            throw new IllegalStateException("Event bus failed", failure);
        }
        accepting.set(true);
    }

    boolean begin() {
        if (!canPublish()) {
            return false;
        }
        publisherCount.incrementAndGet();
        if (!canPublish()) {
            publisherCount.decrementAndGet();
            return false;
        }
        return true;
    }

    void end() {
        publisherCount.decrementAndGet();
    }

    boolean canPublish() {
        return !closed.get() && !failed.get() && accepting.get();
    }

    boolean close() {
        if (!closed.compareAndSet(false, true)) {
            return false;
        }
        accepting.set(false);
        awaitPublishers();
        return true;
    }

    boolean fail(Throwable failure) {
        if (!failed.compareAndSet(false, true)) {
            return false;
        }
        this.failure = failure;
        accepting.set(false);
        return true;
    }

    boolean closed() {
        return closed.get();
    }

    boolean failed() {
        return failed.get();
    }

    Throwable failure() {
        return failure;
    }

    private void awaitPublishers() {
        long deadline = System.nanoTime() + CLOSE_WAIT_MILLIS * 1_000_000L;
        while (publisherCount.get() != 0 && System.nanoTime() < deadline) {
            LockSupport.parkNanos(WAIT_PARK_NANOS);
        }
        if (publisherCount.get() != 0) {
            logger.warn("Event bus closed with {} publisher(s) still active", publisherCount.get());
        }
    }
}
