package io.effi.rpc.metrics;

import io.effi.rpc.common.util.GenericKey;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Caller Metrics.
 */
public class CallerMetrics {

    public static final GenericKey<CallerMetrics> GENERIC_KEY = GenericKey.valueOf("callerMetrics");

    private LongAdder callCount = new LongAdder();

    private LongAdder successCallCount = new LongAdder();

    private LongAdder failureCallCount = new LongAdder();

    private AtomicReference<Double> averageCallTime = new AtomicReference<>(0.0);

    private AtomicReference<Double> averageSerializationTime = new AtomicReference<>(0.0);

    private AtomicReference<Double> averageDeserializationTime = new AtomicReference<>(0.0);

    private AtomicLong maxCallTime = new AtomicLong();

    private AtomicLong minCallTime = new AtomicLong();

    private LongAdder retryCount = new LongAdder();

    private LongAdder timeoutCount = new LongAdder();

    /**
     * Returns the callCount.
     *
     * @return the callCount
     */
    public LongAdder callCount() {
        return callCount;
    }

    /**
     * Sets the callCount.
     *
     * @param callCount callCount
     */
    public CallerMetrics callCount(LongAdder callCount) {
        this.callCount = callCount;
        return this;
    }

    /**
     * Returns the successCallCount.
     *
     * @return the successCallCount
     */
    public LongAdder successCallCount() {
        return successCallCount;
    }

    /**
     * Sets the successCallCount.
     *
     * @param successCallCount successCallCount
     */
    public CallerMetrics successCallCount(LongAdder successCallCount) {
        this.successCallCount = successCallCount;
        return this;
    }

    /**
     * Returns the failureCallCount.
     *
     * @return the failureCallCount
     */
    public LongAdder failureCallCount() {
        return failureCallCount;
    }

    /**
     * Sets the failureCallCount.
     *
     * @param failureCallCount failureCallCount
     */
    public CallerMetrics failureCallCount(LongAdder failureCallCount) {
        this.failureCallCount = failureCallCount;
        return this;
    }

    /**
     * Returns the averageCallTime.
     *
     * @return the averageCallTime
     */
    public AtomicReference<Double> averageCallTime() {
        return averageCallTime;
    }

    /**
     * Sets the averageCallTime.
     *
     * @param averageCallTime averageCallTime
     */
    public CallerMetrics averageCallTime(AtomicReference<Double> averageCallTime) {
        this.averageCallTime = averageCallTime;
        return this;
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
     * Returns the maxCallTime.
     *
     * @return the maxCallTime
     */
    public AtomicLong maxCallTime() {
        return maxCallTime;
    }

    /**
     * Sets the maxCallTime.
     *
     * @param maxCallTime maxCallTime
     */
    public CallerMetrics maxCallTime(AtomicLong maxCallTime) {
        this.maxCallTime = maxCallTime;
        return this;
    }

    /**
     * Returns the minCallTime.
     *
     * @return the minCallTime
     */
    public AtomicLong minCallTime() {
        return minCallTime;
    }

    /**
     * Sets the minCallTime.
     *
     * @param minCallTime minCallTime
     */
    public CallerMetrics minCallTime(AtomicLong minCallTime) {
        this.minCallTime = minCallTime;
        return this;
    }

    /**
     * Returns the retryCount.
     *
     * @return the retryCount
     */
    public LongAdder retryCount() {
        return retryCount;
    }

    /**
     * Sets the retryCount.
     *
     * @param retryCount retryCount
     */
    public CallerMetrics retryCount(LongAdder retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Returns the timeoutCount.
     *
     * @return the timeoutCount
     */
    public LongAdder timeoutCount() {
        return timeoutCount;
    }

    /**
     * Sets the timeoutCount.
     *
     * @param timeoutCount timeoutCount
     */
    public CallerMetrics timeoutCount(LongAdder timeoutCount) {
        this.timeoutCount = timeoutCount;
        return this;
    }
}
