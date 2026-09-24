package io.effi.rpc.concurrent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Immutable monotonic deadline.
 */
public record Deadline(long deadlineNanos) {

    private static final long NONE = Long.MAX_VALUE;

    public static Deadline none() {
        return new Deadline(NONE);
    }

    public static Deadline after(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout must not be negative");
        }
        long timeoutNanos = unit.toNanos(timeout);
        long now = System.nanoTime();
        try {
            return new Deadline(Math.addExact(now, timeoutNanos));
        } catch (ArithmeticException ignored) {
            return none();
        }
    }

    public boolean isNone() {
        return deadlineNanos == NONE;
    }

    public boolean expired() {
        return !isNone() && System.nanoTime() - deadlineNanos >= 0;
    }

    public long remainingNanos() {
        if (isNone()) {
            return NONE;
        }
        return Math.max(0L, deadlineNanos - System.nanoTime());
    }

    public long remaining(TimeUnit unit) {
        return unit.convert(remainingNanos(), TimeUnit.NANOSECONDS);
    }

    public Duration remainingDuration() {
        return isNone() ? Duration.ofNanos(Long.MAX_VALUE) : Duration.ofNanos(remainingNanos());
    }
}
