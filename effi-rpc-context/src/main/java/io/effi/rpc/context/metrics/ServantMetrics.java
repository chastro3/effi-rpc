package io.effi.rpc.context.metrics;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Servant;
import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.util.GenericKey;

/**
 * Provides the metrics owned by one servant.
 */
public final class ServantMetrics extends PeerMetrics {

    public static final MetricKey REQUEST_COUNT = MetricKey.of("rpc.server.request.count");

    public static final MetricKey REQUEST_DURATION = MetricKey.of("rpc.server.request.duration");

    public static final MetricKey SERIALIZE_DURATION = MetricKey.of("rpc.server.serialize.duration");

    public static final MetricKey DESERIALIZE_DURATION = MetricKey.of("rpc.server.deserialize.duration");

    public static final GenericKey<ServantMetrics> KEY = GenericKey.valueOf("servantMetrics");

    private static final GenericKey<Long> REQUEST_START = GenericKey.valueOf("servantMetrics.requestStart");

    private static final ServantMetrics NOOP = new ServantMetrics("none");

    private MetricCounter requestCount = MetricCounter.NOOP;

    private MetricCounter successCount = MetricCounter.NOOP;

    private MetricCounter failureCount = MetricCounter.NOOP;

    private MetricTimer responseTimer = MetricTimer.NOOP;

    public ServantMetrics(String protocol) {
        super(protocol, SERIALIZE_DURATION, DESERIALIZE_DURATION);
    }

    @Override
    protected void registerSpecific(Metrics metrics) {
        this.requestCount = metrics.counter(key(REQUEST_COUNT));
        this.successCount = metrics.counter(key(REQUEST_COUNT).withTag("status", "success"));
        this.failureCount = metrics.counter(key(REQUEST_COUNT).withTag("status", "failure"));
        this.responseTimer = metrics.timer(key(REQUEST_DURATION));
    }

    /**
     * Starts timing the current request.
     *
     * @param context call context of the request
     */
    public void beginRequest(CallContext<?, ?> context) {
        context.set(REQUEST_START, System.nanoTime());
    }

    /**
     * Records one finished request using the elapsed time of its context.
     *
     * @param context call context of the request
     * @param success whether the request succeeded
     */
    public void recordRequest(CallContext<?, ?> context, boolean success) {
        Long start = context.get(REQUEST_START);
        recordRequest(start == null ? 0L : System.nanoTime() - start, success);
    }

    /**
     * Records one finished request.
     *
     * @param durationNanos elapsed nanoseconds
     * @param success whether the request succeeded
     */
    public void recordRequest(long durationNanos, boolean success) {
        requestCount.increment();
        (success ? successCount : failureCount).increment();
        responseTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Returns the servant metrics of the supplied peer, or a shared no-op instance when absent.
     *
     * @param servant call servant
     * @return servant metrics
     */
    public static ServantMetrics of(Servant servant) {
        ServantMetrics metrics = servant == null ? null : servant.get(KEY);
        return metrics == null ? NOOP : metrics;
    }
}
