package io.effi.rpc.metrics;

import io.effi.rpc.common.util.GenericKey;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Callee Metrics.
 */
public class CalleeMetrics {

    public static final GenericKey<CalleeMetrics> GENERIC_KEY = GenericKey.valueOf("calleeMetrics");

    private LongAdder requestCount = new LongAdder();

    private LongAdder successCount = new LongAdder();

    private LongAdder failureCount = new LongAdder();

    private AtomicReference<Double> averageResponseTime = new AtomicReference<>(0.0);

    private AtomicReference<Double> averageSerializationTime = new AtomicReference<>(0.0);

    private AtomicReference<Double> averageDeserializationTime = new AtomicReference<>(0.0);

    private AtomicLong maxResponseTime = new AtomicLong();

    private AtomicLong minResponseTime = new AtomicLong();

    /**
     * Returns the requestCount.
     *
     * @return the requestCount
     */
    public LongAdder requestCount() {
        return requestCount;
    }

    /**
     * Sets the requestCount.
     *
     * @param requestCount requestCount
     */
    public CalleeMetrics requestCount(LongAdder requestCount) {
        this.requestCount = requestCount;
        return this;
    }

    /**
     * Returns the successCount.
     *
     * @return the successCount
     */
    public LongAdder successCount() {
        return successCount;
    }

    /**
     * Sets the successCount.
     *
     * @param successCount successCount
     */
    public CalleeMetrics successCount(LongAdder successCount) {
        this.successCount = successCount;
        return this;
    }

    /**
     * Returns the failureCount.
     *
     * @return the failureCount
     */
    public LongAdder failureCount() {
        return failureCount;
    }

    /**
     * Sets the failureCount.
     *
     * @param failureCount failureCount
     */
    public CalleeMetrics failureCount(LongAdder failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    /**
     * Returns the averageResponseTime.
     *
     * @return the averageResponseTime
     */
    public AtomicReference<Double> averageResponseTime() {
        return averageResponseTime;
    }

    /**
     * Returns the averageSerializationTime.
     *
     * @return the averageSerializationTime
     */
    public AtomicReference<Double> averageSerializationTime() {
        return averageSerializationTime;
    }

    /**
     * Returns the averageDeserializationTime.
     *
     * @return the averageDeserializationTime
     */
    public AtomicReference<Double> averageDeserializationTime() {
        return averageDeserializationTime;
    }

    /**
     * Sets the averageResponseTime.
     *
     * @param averageResponseTime averageResponseTime
     */
    public CalleeMetrics averageResponseTime(AtomicReference<Double> averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
        return this;
    }

    /**
     * Returns the maxResponseTime.
     *
     * @return the maxResponseTime
     */
    public AtomicLong maxResponseTime() {
        return maxResponseTime;
    }

    /**
     * Sets the maxResponseTime.
     *
     * @param maxResponseTime maxResponseTime
     */
    public CalleeMetrics maxResponseTime(AtomicLong maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
        return this;
    }

    /**
     * Returns the minResponseTime.
     *
     * @return the minResponseTime
     */
    public AtomicLong minResponseTime() {
        return minResponseTime;
    }

    /**
     * Sets the minResponseTime.
     *
     * @param minResponseTime minResponseTime
     */
    public CalleeMetrics minResponseTime(AtomicLong minResponseTime) {
        this.minResponseTime = minResponseTime;
        return this;
    }

    @Override
    public String toString() {
        return "CalleeMetrics{"
                + "requestCount=" + requestCount
                + ", successCount=" + successCount
                + ", failureCount=" + failureCount
                + ", averageResponseTime=" + averageResponseTime
                + ", averageSerializationTime=" + averageSerializationTime
                + ", averageDeserializationTime=" + averageDeserializationTime
                + ", maxResponseTime=" + maxResponseTime
                + ", minResponseTime=" + minResponseTime
                + '}';
    }

}
