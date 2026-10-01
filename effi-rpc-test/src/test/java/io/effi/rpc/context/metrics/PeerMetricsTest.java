package io.effi.rpc.context.metrics;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.metrics.TimerSample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerMetricsTest {

    @Test
    void recordsCallDurationFromContext() {
        DefaultMetrics registry = new DefaultMetrics(ScopedPlatform.defaultInstance());
        CallerMetrics metrics = new CallerMetrics("test");
        registry.register(metrics);

        CallContext<Request, Caller<?>> context = new CallContext<>(null, null, null, null, new Object[0]);
        metrics.beginCall(context);
        metrics.recordCall(context, true);

        assertEquals(1L, registry.counter(CallerMetrics.CALL_COUNT.withTag("protocol", "test")).count());
        TimerSample duration = registry.timer(CallerMetrics.CALL_DURATION.withTag("protocol", "test")).snapshot();
        assertEquals(1L, duration.count());
    }

    @Test
    void recordsFailedRequestFromContext() {
        DefaultMetrics registry = new DefaultMetrics(ScopedPlatform.defaultInstance());
        ServantMetrics metrics = new ServantMetrics("test");
        registry.register(metrics);

        CallContext<Request, Servant> context = new CallContext<>(null, null, null, null, new Object[0]);
        metrics.beginRequest(context);
        metrics.recordRequest(context, false);

        assertEquals(1L, registry.counter(ServantMetrics.REQUEST_COUNT.withTag("protocol", "test")).count());
        assertEquals(1L, registry.counter(
                ServantMetrics.REQUEST_COUNT.withTag("protocol", "test").withTag("status", "failure")
        ).count());
        assertEquals(1L, registry.timer(
                ServantMetrics.REQUEST_DURATION.withTag("protocol", "test")
        ).snapshot().count());
    }

    @Test
    void noopMetricsAcceptRecording() {
        CallerMetrics callerMetrics = CallerMetrics.of(null);
        callerMetrics.recordCall(10L, true);
        callerMetrics.recordRetry();
        callerMetrics.recordTimeout();
        ServantMetrics.of(null).recordRequest(10L, true);

        assertEquals(0D, new CallerMetrics("test").averageSerializationNanos());
        assertEquals(0D, new ServantMetrics("test").averageDeserializationNanos());
    }
}
