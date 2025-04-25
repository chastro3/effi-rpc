package io.effi.rpc.metrics.event;

import io.effi.rpc.event.AbstractEvent;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.metrics.constant.MetricsKey;

/**
 * Metrics Event.
 *
 * @param <T>
 */

public class MetricsEvent<T> extends AbstractEvent<T> {

    private final boolean hasException;

    private final long serializationDuration;

    private final long deserializationDuration;

    private final long executeDuration;

    public MetricsEvent(T source, InvocationContext<?, ?> context, boolean hasException) {
        super(source);
        this.hasException = hasException;
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
     * Returns the hasException.
     *
     * @return the hasException
     */
    public boolean hasException() {
        return hasException;
    }

    /**
     * Returns the executeDuration.
     *
     * @return the executeDuration
     */
    public long executeDuration() {
        return executeDuration;
    }

    /**
     * Returns the serializationDuration.
     *
     * @return the serializationDuration
     */
    public long serializationDuration() {
        return serializationDuration;
    }

    /**
     * Returns the deserializationDuration.
     *
     * @return the deserializationDuration
     */
    public long deserializationDuration() {
        return deserializationDuration;
    }
}
