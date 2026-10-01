package io.effi.rpc.context.metrics.event;

import io.effi.rpc.component.event.EventHandler;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.util.AtomicUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * Handles caller metrics events.
 */
public class CallerMetricsEventHandler implements EventHandler<CallerMetricsEvent> {

    @Override
    public void onEvent(CallerMetricsEvent event) {
        CallerMetrics callerMetrics = event.source();
        callerMetrics.callCount().increment();
        if (!event.succeeded()) {
            callerMetrics.failureCallCount().increment();
            return;
        }
        callerMetrics.successCallCount().increment();
        long executeDuration = event.executeDuration();
        long serializationDuration = event.serializationDuration();
        long deserializationDuration = event.deserializationDuration();
        AtomicUtil.updateAtomicLong(callerMetrics.maxCallTime(), old -> Math.max(old, executeDuration));
        AtomicUtil.updateAtomicLong(callerMetrics.minCallTime(),
                old -> old == 0 ? executeDuration : Math.min(old, executeDuration));
        AtomicUtil.updateAtomicReference(callerMetrics.averageCallTime(), old -> {
            LongAdder successCallCount = callerMetrics.successCallCount();
            double totalSuccessTime = (successCallCount.doubleValue() - 1) * old;
            return (totalSuccessTime + executeDuration) / successCallCount.doubleValue();
        });
        AtomicUtil.updateAtomicReference(callerMetrics.averageSerializationTime(), old -> {
            LongAdder callCount = callerMetrics.callCount();
            double totalSerializeTime = (callCount.doubleValue() - 1) * old;
            return (totalSerializeTime + serializationDuration) / callCount.doubleValue();
        });
        AtomicUtil.updateAtomicReference(callerMetrics.averageDeserializationTime(), old -> {
            LongAdder callCount = callerMetrics.callCount();
            double totalDeserializeTime = (callCount.doubleValue() - 1) * old;
            return (totalDeserializeTime + deserializationDuration) / callCount.doubleValue();
        });
    }
}
