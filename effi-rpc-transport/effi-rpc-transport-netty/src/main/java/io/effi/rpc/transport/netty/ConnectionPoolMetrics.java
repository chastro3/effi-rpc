package io.effi.rpc.transport.netty;

import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;
import io.effi.rpc.util.AssertUtil;

import java.util.function.IntSupplier;

/**
 * Registers and records connection-pool metrics for one client endpoint.
 */
public final class ConnectionPoolMetrics implements MetricsRegistrar {

    public static final MetricKey ACQUIRED_CONNECTIONS = MetricKey.of("rpc.transport.connection.acquired");

    public static final MetricKey MAX_CONNECTIONS = MetricKey.of("rpc.transport.connection.max");

    public static final MetricKey ACQUIRE_FAILURE_COUNT = MetricKey.of("rpc.transport.connection.acquire.failure.count");

    private final String endpoint;

    private final IntSupplier acquired;

    private final IntSupplier max;

    private MetricCounter acquireFailures = MetricCounter.NOOP;

    public ConnectionPoolMetrics(String endpoint, IntSupplier acquired, IntSupplier max) {
        this.endpoint = AssertUtil.notBlank(endpoint, "endpoint");
        this.acquired = AssertUtil.notNull(acquired, "acquired");
        this.max = AssertUtil.notNull(max, "max");
    }

    @Override
    public void register(Metrics metrics) {
        this.acquireFailures = metrics.counter(ACQUIRE_FAILURE_COUNT.withTag("endpoint", endpoint));
        metrics.gauge(ACQUIRED_CONNECTIONS.withTag("endpoint", endpoint), acquired::getAsInt);
        metrics.gauge(MAX_CONNECTIONS.withTag("endpoint", endpoint), max::getAsInt);
    }

    /**
     * Records one failed connection acquisition.
     */
    public void acquireFailed() {
        acquireFailures.increment();
    }
}
