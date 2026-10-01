package io.effi.rpc.context.metrics;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.metrics.TimerSample;
import io.effi.rpc.transport.TransportSupport;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void lookupFallsBackToNoopWhenPeerHasNoMetrics() {
        Caller<?> caller = peer(Caller.class, null, 0L);
        Servant servant = peer(Servant.class, null, 0L);

        CallerMetrics callerMetrics = CallerMetrics.of(caller);
        callerMetrics.recordCall(10L, true);
        callerMetrics.recordRetry();
        callerMetrics.recordTimeout();

        ServantMetrics servantMetrics = ServantMetrics.of(servant);
        servantMetrics.recordRequest(10L, true);
        servantMetrics.recordRequest(10L, false);
    }

    @Test
    void transportTreatsMissingOrUnregisteredMetricsAsUnmeasured() {
        Peer missing = peer(Peer.class, null, 1_000L);
        assertTrue(TransportSupport.inIOSerialization(missing));
        assertTrue(TransportSupport.inIODeserialization(missing));

        Peer unregistered = peer(Peer.class, new CallerMetrics("test"), 1_000L);
        assertTrue(TransportSupport.inIOSerialization(unregistered));
        assertTrue(TransportSupport.inIODeserialization(unregistered));
    }

    @Test
    void transportComparesRecordedAverageWithThreshold() {
        DefaultMetrics registry = new DefaultMetrics(ScopedPlatform.defaultInstance());
        CallerMetrics metrics = new CallerMetrics("test");
        registry.register(metrics);

        metrics.recordSerialization(200L);
        metrics.recordDeserialization(200L);
        Peer fast = peer(Peer.class, metrics, 1_000L);
        assertTrue(TransportSupport.inIOSerialization(fast));
        assertTrue(TransportSupport.inIODeserialization(fast));

        metrics.recordSerialization(5_000L);
        metrics.recordDeserialization(5_000L);
        Peer slow = peer(Peer.class, metrics, 1_000L);
        assertFalse(TransportSupport.inIOSerialization(slow));
        assertFalse(TransportSupport.inIODeserialization(slow));
    }

    @SuppressWarnings("unchecked")
    private static <P extends Peer> P peer(Class<P> type, PeerMetrics metrics, long threshold) {
        return (P) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        if (args[0] == CallerMetrics.KEY && metrics instanceof CallerMetrics) {
                            return metrics;
                        }
                        if (args[0] == ServantMetrics.KEY && metrics instanceof ServantMetrics) {
                            return metrics;
                        }
                        return null;
                    }
                    if ("option".equals(method.getName())) {
                        return threshold;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }
}
