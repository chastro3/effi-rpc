package io.effi.rpc.governance.metrics;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.metrics.CounterSample;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricsSnapshot;
import io.effi.rpc.metrics.TimerSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GovernanceMetricsTest {

    @Test
    void recordsDiscoveryRouteAndLoadBalancerMetrics() {
        ScopedPlatform platform = new ScopedPlatform("governance-metrics-platform");
        DefaultMetrics metrics = new DefaultMetrics(platform);
        GovernanceMetrics governanceMetrics = new GovernanceMetrics();
        metrics.register(governanceMetrics);

        try {
            governanceMetrics.recordDiscoverySuccess(1_000L, 2);
            governanceMetrics.recordDiscoveryFailure(2_000L);
            governanceMetrics.recordDiscoveryEmpty(2_500L);
            governanceMetrics.recordRoute(3_000L, 2, 1);
            governanceMetrics.recordRoute(4_000L, 1, 0);
            governanceMetrics.recordRouteFailure(4_500L);
            governanceMetrics.recordLoadBalancer("random", 500L);
            governanceMetrics.recordLoadBalancer("random", 700L);

            MetricsSnapshot snapshot = metrics.snapshot();

            assertEquals(1L, counter(snapshot, GovernanceMetrics.DISCOVERY_COUNT.withTag("result", "success")));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.DISCOVERY_COUNT.withTag("result", "failure")));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.DISCOVERY_COUNT.withTag("result", "empty")));
            assertEquals(2L, counter(snapshot, GovernanceMetrics.DISCOVERY_INSTANCE_COUNT));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.ROUTE_COUNT.withTag("result", "matched")));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.ROUTE_COUNT.withTag("result", "empty")));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.ROUTE_COUNT.withTag("result", "failure")));
            assertEquals(3L, counter(snapshot, GovernanceMetrics.ROUTE_CANDIDATE_COUNT.withTag("stage", "input")));
            assertEquals(1L, counter(snapshot, GovernanceMetrics.ROUTE_CANDIDATE_COUNT.withTag("stage", "output")));
            assertEquals(2L, counter(snapshot, GovernanceMetrics.LOAD_BALANCER_COUNT.withTag("strategy", "random")));
            assertEquals(3L, timerCount(snapshot, GovernanceMetrics.DISCOVERY_DURATION));
        } finally {
            metrics.close();
            platform.close();
        }
    }

    private static long counter(MetricsSnapshot snapshot, MetricKey key) {
        return snapshot.samples().stream()
                .filter(CounterSample.class::isInstance)
                .map(CounterSample.class::cast)
                .filter(sample -> sample.key().equals(key))
                .mapToLong(CounterSample::value)
                .findFirst()
                .orElse(0L);
    }

    private static long timerCount(MetricsSnapshot snapshot, MetricKey key) {
        return snapshot.samples().stream()
                .filter(TimerSample.class::isInstance)
                .map(TimerSample.class::cast)
                .filter(sample -> sample.key().equals(key))
                .mapToLong(TimerSample::count)
                .findFirst()
                .orElse(0L);
    }
}
