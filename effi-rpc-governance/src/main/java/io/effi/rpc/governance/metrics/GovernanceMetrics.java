package io.effi.rpc.governance.metrics;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.metrics.MetricCounter;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;
import io.effi.rpc.util.StringUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Registers governance metrics for discovery, routing, and load balancing.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public final class GovernanceMetrics implements MetricsRegistrar {

    public static final MetricKey DISCOVERY_COUNT = MetricKey.of("governance.discovery.count");

    public static final MetricKey DISCOVERY_DURATION = MetricKey.of("governance.discovery.duration");

    public static final MetricKey DISCOVERY_INSTANCE_COUNT = MetricKey.of("governance.discovery.instance.count");

    public static final MetricKey ROUTE_COUNT = MetricKey.of("governance.route.count");

    public static final MetricKey ROUTE_DURATION = MetricKey.of("governance.route.duration");

    public static final MetricKey ROUTE_CANDIDATE_COUNT = MetricKey.of("governance.route.candidate.count");

    public static final MetricKey LOAD_BALANCER_COUNT = MetricKey.of("governance.lb.selection.count");

    public static final MetricKey LOAD_BALANCER_DURATION = MetricKey.of("governance.lb.selection.duration");
    private final ConcurrentMap<String, LoadBalancerInstruments> loadBalancerInstruments = new ConcurrentHashMap<>();
    private volatile Metrics metrics;
    private MetricTimer discoveryTimer = MetricTimer.NOOP;

    private MetricCounter discoverySuccess = MetricCounter.NOOP;

    private MetricCounter discoveryFailure = MetricCounter.NOOP;

    private MetricCounter discoveryEmpty = MetricCounter.NOOP;

    private MetricCounter discoveredInstances = MetricCounter.NOOP;

    private MetricTimer routeTimer = MetricTimer.NOOP;

    private MetricCounter routeMatched = MetricCounter.NOOP;

    private MetricCounter routeEmpty = MetricCounter.NOOP;

    private MetricCounter routeFailure = MetricCounter.NOOP;

    private MetricCounter routeCandidatesInput = MetricCounter.NOOP;

    private MetricCounter routeCandidatesOutput = MetricCounter.NOOP;

    @Override
    public void register(Metrics metrics) {
        this.metrics = metrics;
        this.discoveryTimer = metrics.timer(DISCOVERY_DURATION);
        this.discoverySuccess = metrics.counter(DISCOVERY_COUNT.withTag("result", "success"));
        this.discoveryFailure = metrics.counter(DISCOVERY_COUNT.withTag("result", "failure"));
        this.discoveryEmpty = metrics.counter(DISCOVERY_COUNT.withTag("result", "empty"));
        this.discoveredInstances = metrics.counter(DISCOVERY_INSTANCE_COUNT);
        this.routeTimer = metrics.timer(ROUTE_DURATION);
        this.routeMatched = metrics.counter(ROUTE_COUNT.withTag("result", "matched"));
        this.routeEmpty = metrics.counter(ROUTE_COUNT.withTag("result", "empty"));
        this.routeFailure = metrics.counter(ROUTE_COUNT.withTag("result", "failure"));
        this.routeCandidatesInput = metrics.counter(ROUTE_CANDIDATE_COUNT.withTag("stage", "input"));
        this.routeCandidatesOutput = metrics.counter(ROUTE_CANDIDATE_COUNT.withTag("stage", "output"));
        this.loadBalancerInstruments.clear();
    }

    /**
     * Records one successful discovery.
     *
     * @param durationNanos discovery duration in nanoseconds
     * @param instanceCount number of discovered instances
     */
    public void recordDiscoverySuccess(long durationNanos, int instanceCount) {
        discoverySuccess.increment();
        discoveredInstances.add(instanceCount);
        discoveryTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one failed discovery.
     *
     * @param durationNanos discovery duration in nanoseconds
     */
    public void recordDiscoveryFailure(long durationNanos) {
        discoveryFailure.increment();
        discoveryTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one discovery that completed without finding any instance.
     *
     * @param durationNanos discovery duration in nanoseconds
     */
    public void recordDiscoveryEmpty(long durationNanos) {
        discoveryEmpty.increment();
        discoveryTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one routing pass.
     *
     * @param durationNanos routing duration in nanoseconds
     * @param inputCount    candidate count before routing
     * @param outputCount   candidate count after routing
     */
    public void recordRoute(long durationNanos, int inputCount, int outputCount) {
        (outputCount == 0 ? routeEmpty : routeMatched).increment();
        routeCandidatesInput.add(inputCount);
        routeCandidatesOutput.add(outputCount);
        routeTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one failed routing pass.
     *
     * @param durationNanos routing duration in nanoseconds
     */
    public void recordRouteFailure(long durationNanos) {
        routeFailure.increment();
        routeTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records one load balancer selection.
     *
     * @param strategy      load balancer strategy name
     * @param durationNanos selection duration in nanoseconds
     */
    public void recordLoadBalancer(String strategy, long durationNanos) {
        String name = StringUtil.isBlank(strategy) ? "unknown" : strategy;
        LoadBalancerInstruments instruments = loadBalancerInstruments
                .computeIfAbsent(name, this::newLoadBalancerInstruments);
        instruments.count().increment();
        instruments.timer().recordNanos(Math.max(0L, durationNanos));
    }

    private LoadBalancerInstruments newLoadBalancerInstruments(String strategy) {
        Metrics current = metrics;
        if (current == null) {
            return new LoadBalancerInstruments(MetricCounter.NOOP, MetricTimer.NOOP);
        }
        return new LoadBalancerInstruments(
                current.counter(LOAD_BALANCER_COUNT.withTag("strategy", strategy)),
                current.timer(LOAD_BALANCER_DURATION.withTag("strategy", strategy))
        );
    }

    private record LoadBalancerInstruments(MetricCounter count, MetricTimer timer) {
    }
}
