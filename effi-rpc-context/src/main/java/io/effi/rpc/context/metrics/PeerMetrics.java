package io.effi.rpc.context.metrics;

import io.effi.rpc.context.Peer;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.GenericKey;

/**
 * Defines the metrics shared by caller and servant sides.
 */
public abstract class PeerMetrics implements MetricsRegistrar {

    public static final GenericKey<PeerMetrics> KEY = GenericKey.valueOf("peerMetrics");

    private final String protocol;

    private final MetricKey serializationKey;

    private final MetricKey deserializationKey;

    private MetricTimer serializationTimer = MetricTimer.NOOP;

    private MetricTimer deserializationTimer = MetricTimer.NOOP;

    protected PeerMetrics(String protocol, MetricKey serializationKey, MetricKey deserializationKey) {
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.serializationKey = AssertUtil.notNull(serializationKey, "serializationKey");
        this.deserializationKey = AssertUtil.notNull(deserializationKey, "deserializationKey");
    }

    @Override
    public final void register(Metrics metrics) {
        AssertUtil.notNull(metrics, "metrics");
        this.serializationTimer = metrics.timer(key(serializationKey));
        this.deserializationTimer = metrics.timer(key(deserializationKey));
        registerSpecific(metrics);
    }

    protected abstract void registerSpecific(Metrics metrics);

    protected final MetricKey key(MetricKey metric) {
        return metric.withTag("protocol", protocol);
    }

    public final void recordSerialization(long durationNanos) {
        serializationTimer.recordNanos(Math.max(0L, durationNanos));
    }

    public final void recordDeserialization(long durationNanos) {
        deserializationTimer.recordNanos(Math.max(0L, durationNanos));
    }

    public final double averageSerializationNanos() {
        return averageNanos(serializationTimer);
    }

    public final double averageDeserializationNanos() {
        return averageNanos(deserializationTimer);
    }

    public static PeerMetrics of(Peer peer) {
        return peer == null ? null : peer.get(KEY);
    }

    private static double averageNanos(MetricTimer timer) {
        var snapshot = timer.snapshot();
        return snapshot.count() == 0L
                ? 0D
                : (double) snapshot.totalNanos() / snapshot.count();
    }
}
