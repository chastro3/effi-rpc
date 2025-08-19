package io.effi.rpc.context.metrics.event;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.component.event.AbstractEvent;
import io.effi.rpc.context.metrics.constant.MetricsKey;

/**
 * Metrics Event.
 *
 * @param <T>
 */

public class MetricsEvent<T> extends AbstractEvent<T> {

    private final boolean succeeded;

    private final long serializationDuration;

    private final long deserializationDuration;

    private final long executeDuration;

    public MetricsEvent(T source, CallContext<?, ?> context, boolean succeeded) {
        super(source);
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
     * Returns the hasException.
     *
     * @return the hasException
     */
    public boolean succeeded() {
        return succeeded;
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
