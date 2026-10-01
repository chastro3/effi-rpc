package io.effi.rpc.context.metrics.event;

import io.effi.rpc.component.event.Event;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.metrics.constant.MetricsKey;

/**
 * Carries call metrics produced by caller or callee interceptors.
 *
 * @param <T> metrics source type
 */
public abstract class MetricsEvent<T> implements Event {

    private final T source;

    private final boolean succeeded;

    private final long serializationDuration;

    private final long deserializationDuration;

    private final long executeDuration;

    public MetricsEvent(T source, CallContext<?, ?> context, boolean succeeded) {
        this.source = source;
        this.succeeded = succeeded;
        Long startTime = context.get(MetricsKey.START_TIME);
        Long endTime = context.get(MetricsKey.END_TIME);
        this.executeDuration = (endTime - startTime) / 1000_000;
        Long serializationStartTime = context.get(MetricsKey.SERIALIZE_START_TIME);
        Long serializationEndTime = context.get(MetricsKey.SERIALIZE_END_TIME);
        this.serializationDuration = serializationEndTime - serializationStartTime;
        Long deserializationStartTime = context.get(MetricsKey.DESERIALIZE_START_TIME);
        Long deserializationEndTime = context.get(MetricsKey.DESERIALIZE_END_TIME);
        this.deserializationDuration = deserializationEndTime - deserializationStartTime;
    }

    /**
     * Returns the metrics source.
     *
     * @return metrics source
     */
    public T source() {
        return source;
    }

    /**
     * Returns whether the call succeeded.
     *
     * @return {@code true} when the call succeeded
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * Returns the execution duration in nanoseconds.
     *
     * @return execution duration
     */
    public long executeDuration() {
        return executeDuration;
    }

    /**
     * Returns the serialization duration in nanoseconds.
     *
     * @return serialization duration
     */
    public long serializationDuration() {
        return serializationDuration;
    }

    /**
     * Returns the deserialization duration in nanoseconds.
     *
     * @return deserialization duration
     */
    public long deserializationDuration() {
        return deserializationDuration;
    }
}
