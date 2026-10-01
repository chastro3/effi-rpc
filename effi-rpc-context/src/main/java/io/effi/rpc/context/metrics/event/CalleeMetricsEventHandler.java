package io.effi.rpc.context.metrics.event;

import io.effi.rpc.component.event.EventHandler;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.util.AtomicUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * Handles callee metrics events.
 */
public class CalleeMetricsEventHandler implements EventHandler<CalleeMetricsEvent> {

    @Override
    public void onEvent(CalleeMetricsEvent event) {
        CalleeMetrics calleeMetrics = event.source();
        calleeMetrics.requestCount().increment();
        if (!event.succeeded()) {
            calleeMetrics.failureCount().increment();
            return;
        }
        calleeMetrics.successCount().increment();
        long executeDuration = event.executeDuration();
        long serializationDuration = event.serializationDuration();
        long deserializationDuration = event.deserializationDuration();
        AtomicUtil.updateAtomicLong(calleeMetrics.maxResponseTime(), old -> Math.max(old, executeDuration));
        AtomicUtil.updateAtomicLong(calleeMetrics.minResponseTime(),
                old -> old == 0 ? executeDuration : Math.min(old, executeDuration));
        AtomicUtil.updateAtomicReference(calleeMetrics.averageResponseTime(), old -> {
            LongAdder successCount = calleeMetrics.successCount();
            double totalSuccessTime = (successCount.doubleValue() - 1) * old;
            return (totalSuccessTime + executeDuration) / successCount.doubleValue();
        });
        AtomicUtil.updateAtomicReference(calleeMetrics.averageSerializationTime(), old -> {
            LongAdder callCount = calleeMetrics.requestCount();
            double totalSerializeTime = (callCount.doubleValue() - 1) * old;
            return (totalSerializeTime + serializationDuration) / callCount.doubleValue();
        });
        AtomicUtil.updateAtomicReference(calleeMetrics.averageDeserializationTime(), old -> {
            LongAdder callCount = calleeMetrics.requestCount();
            double totalDeserializeTime = (callCount.doubleValue() - 1) * old;
            return (totalDeserializeTime + deserializationDuration) / callCount.doubleValue();
        });
    }
}
