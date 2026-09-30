package io.effi.rpc.concurrent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Defines an immutable monotonic deadline.
 */
public record Deadline(long deadlineNanos) {

    private static final long NONE = Long.MAX_VALUE;

    /**
     * Returns a deadline that never expires.
     */
    public static Deadline none() {
        return new Deadline(NONE);
    }

    /**
     * Returns a deadline offset from the current monotonic time.
     *
     * @param timeout non-negative timeout
     * @param unit timeout unit
     * @return deadline after the timeout, or a disabled deadline when nanos overflow
     * @throws IllegalArgumentException if the timeout is negative
     */
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

    /**
     * Indicates whether this deadline is disabled.
     */
    public boolean isNone() {
        return deadlineNanos == NONE;
    }

    /**
     * Indicates whether this deadline has elapsed.
     */
    public boolean expired() {
        return !isNone() && System.nanoTime() - deadlineNanos >= 0;
    }

    /**
     * Returns the remaining nanoseconds, or {@link Long#MAX_VALUE} when disabled.
     */
    public long remainingNanos() {
        if (isNone()) {
            return NONE;
        }
        return Math.max(0L, deadlineNanos - System.nanoTime());
    }

    /**
     * Returns the remaining time in the requested unit.
     *
     * @param unit target time unit
     * @return remaining time, truncated to the requested unit
     */
    public long remaining(TimeUnit unit) {
        return unit.convert(remainingNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the remaining duration, or the maximum representable nanosecond duration when disabled.
     */
    public Duration remainingDuration() {
        return isNone() ? Duration.ofNanos(Long.MAX_VALUE) : Duration.ofNanos(remainingNanos());
    }
}
