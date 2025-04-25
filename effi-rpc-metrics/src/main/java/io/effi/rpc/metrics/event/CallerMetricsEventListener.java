package io.effi.rpc.metrics.event;

import io.effi.rpc.event.EventListener;
import io.effi.rpc.util.AtomicUtil;
import io.effi.rpc.metrics.CallerMetrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * CalleeMetricsEvent Listener.
 */
public class CallerMetricsEventListener implements EventListener<CallerMetricsEvent> {

    @Override
    public void onEvent(CallerMetricsEvent event) {
        CallerMetrics callerMetrics = event.source();
        callerMetrics.callCount().increment();
        if (event.hasException()) {
            callerMetrics.failureCallCount().increment();
        } else {
            callerMetrics.successCallCount().increment();
            long executeDuration = event.executeDuration();
            long serializationDuration = event.serializationDuration();
            long deserializationDuration = event.deserializationDuration();
            AtomicUtil.updateAtomicLong(callerMetrics.maxCallTime(), old -> Math.max(old, executeDuration));
            AtomicUtil.updateAtomicLong(callerMetrics.minCallTime(), old -> old == 0 ? executeDuration : Math.min(old, executeDuration));
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
                double totalSerializeTime = (callCount.doubleValue() - 1) * old;
                return (totalSerializeTime + deserializationDuration) / callCount.doubleValue();
            });
        }
    }
}
