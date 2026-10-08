package io.effi.rpc.context.metrics;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.util.GenericKey;

/**
 * Provides the metrics owned by one caller.
 */
public final class CallerMetrics extends PeerMetrics {

    public static final MetricKey CALL_COUNT = MetricKey.of("rpc.client.call.count");

    public static final MetricKey CALL_DURATION = MetricKey.of("rpc.client.call.duration");

    public static final MetricKey SERIALIZE_DURATION = MetricKey.of("rpc.client.serialize.duration");

    public static final MetricKey DESERIALIZE_DURATION = MetricKey.of("rpc.client.deserialize.duration");

    public static final MetricKey RETRY_COUNT = MetricKey.of("rpc.client.retry.count");

    public static final MetricKey TIMEOUT_COUNT = MetricKey.of("rpc.client.timeout.count");

    public static final GenericKey<CallerMetrics> KEY = GenericKey.valueOf("callerMetrics");

    private static final GenericKey<Long> CALL_START = GenericKey.valueOf("callerMetrics.callStart");

    private static final CallerMetrics NOOP = new CallerMetrics("none");

    private MetricCounter callCount = MetricCounter.NOOP;

    private MetricCounter successCount = MetricCounter.NOOP;

    private MetricCounter failureCount = MetricCounter.NOOP;

    private MetricCounter retryCount = MetricCounter.NOOP;

    private MetricCounter timeoutCount = MetricCounter.NOOP;

    private MetricTimer callTimer = MetricTimer.NOOP;

    public CallerMetrics(String protocol) {
        super(protocol, SERIALIZE_DURATION, DESERIALIZE_DURATION);
    }

    /**
     * Returns the caller metrics of the supplied peer, or a shared no-op instance when absent.
     *
     * @param caller call caller
     * @return caller metrics
     */
    public static CallerMetrics of(Caller<?> caller) {
        CallerMetrics metrics = caller == null ? null : caller.get(KEY);
        return metrics == null ? NOOP : metrics;
    }

    /**
     * Starts timing the current call attempt.
     *
     * @param context call context of the attempt
     */
    public void beginCall(CallContext<?, ?> context) {
        context.set(CALL_START, System.nanoTime());
    }

    /**
     * Records one finished call attempt using the elapsed time of its context.
     *
     * @param context call context of the attempt
     * @param success whether the attempt succeeded
     */
    public void recordCall(CallContext<?, ?> context, boolean success) {
        Long start = context.get(CALL_START);
        recordCall(start == null ? 0L : System.nanoTime() - start, success);
    }

    /**
     * Records one finished call attempt.
     *
     * @param durationNanos elapsed nanoseconds
     * @param success whether the attempt succeeded
     */
    public void recordCall(long durationNanos, boolean success) {
        callCount.increment();
        (success ? successCount : failureCount).increment();
        callTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one scheduled retry.
     */
    public void recordRetry() {
        retryCount.increment();
    }

    /**
     * Records one expired deadline.
     */
    public void recordTimeout() {
        timeoutCount.increment();
    }

    @Override
    protected void registerSpecific(Metrics metrics) {
        this.callCount = metrics.counter(key(CALL_COUNT));
        this.successCount = metrics.counter(key(CALL_COUNT).withTag("status", "success"));
        this.failureCount = metrics.counter(key(CALL_COUNT).withTag("status", "failure"));
        this.retryCount = metrics.counter(key(RETRY_COUNT));
        this.timeoutCount = metrics.counter(key(TIMEOUT_COUNT));
        this.callTimer = metrics.timer(key(CALL_DURATION));
    }
}
