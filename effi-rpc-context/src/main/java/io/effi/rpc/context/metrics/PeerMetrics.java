package io.effi.rpc.context.metrics;

import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.MetricTimer;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsRegistrar;
import io.effi.rpc.util.AssertUtil;

/**
 * Defines the metrics shared by caller and servant sides.
 */
public abstract class PeerMetrics implements MetricsRegistrar {

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

    protected final MetricKey key(MetricKey metric) {
        return metric.withTag("protocol", protocol);
    }

    protected abstract void registerSpecific(Metrics metrics);

    /**
     * Records the serialization duration of one operation.
     *
     * @param durationNanos elapsed nanoseconds
     */
    public final void recordSerialization(long durationNanos) {
        serializationTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Records the deserialization duration of one operation.
     *
     * @param durationNanos elapsed nanoseconds
     */
    public final void recordDeserialization(long durationNanos) {
        deserializationTimer.recordNanos(Math.max(0L, durationNanos));
    }

    /**
     * Returns the average serialization duration.
     */
    public final double averageSerializationNanos() {
        return serializationTimer.snapshot().averageNanos();
    }

    /**
     * Returns the average deserialization duration.
     */
    public final double averageDeserializationNanos() {
        return deserializationTimer.snapshot().averageNanos();
    }
}
