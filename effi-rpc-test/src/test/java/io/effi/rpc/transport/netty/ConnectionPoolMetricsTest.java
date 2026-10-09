package io.effi.rpc.transport.netty;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.metrics.MetricKey;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionPoolMetricsTest {

    private static final String ENDPOINT = "http/1.1@127.0.0.1:8080";

    @Test
    void registersConnectionPoolMetricsWithEndpointTag() {
        ScopedPlatform platform = new ScopedPlatform("connection-pool-metrics");
        DefaultMetrics metrics = new DefaultMetrics(platform);
        AtomicInteger acquired = new AtomicInteger(3);
        ConnectionPoolMetrics poolMetrics = new ConnectionPoolMetrics(
                ENDPOINT,
                acquired::get,
                () -> 16
        );

        metrics.register(poolMetrics);

        assertEquals(3D, gauge(metrics, ConnectionPoolMetrics.ACQUIRED_CONNECTIONS));
        assertEquals(16D, gauge(metrics, ConnectionPoolMetrics.MAX_CONNECTIONS));

        poolMetrics.acquireFailed();
        assertEquals(1L, metrics.counter(
                ConnectionPoolMetrics.ACQUIRE_FAILURE_COUNT.withTag("endpoint", ENDPOINT)
        ).count());

        metrics.close();
        platform.close();
    }

    private static double gauge(DefaultMetrics metrics, MetricKey key) {
        return metrics.gauge(key.withTag("endpoint", ENDPOINT), () -> -1D).value();
    }
}
