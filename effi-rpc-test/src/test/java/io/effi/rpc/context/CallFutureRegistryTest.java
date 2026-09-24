package io.effi.rpc.context;

import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallFutureRegistryTest {

    @Test
    void registerLookupAndAutoRemove() throws Exception {
        CallFutureRegistry registry = new CallFutureRegistry();
        Promise<String> future = new Promise<>();

        long callId = registry.register(future);

        assertSame(future, registry.lookup(callId));
        assertTrue(future.success("ok"));
        assertNull(registry.lookup(callId));
        assertEquals(0, registry.size());
    }

    @Test
    void cancelCompletesAndRemovesFuture() throws Exception {
        CallFutureRegistry registry = new CallFutureRegistry();
        Promise<String> future = new Promise<>();
        long callId = registry.register(future);
        EffiRpcException reason = PredefinedErrorCode.CALL_CANCELLED.fail("closed");

        assertTrue(registry.cancel(callId, reason));
        assertSame(reason, future.await().cause());
        assertNull(registry.lookup(callId));
    }

    @Test
    void closeCancelsEveryPendingFuture() throws Exception {
        CallFutureRegistry registry = new CallFutureRegistry();
        Promise<String> first = new Promise<>();
        Promise<String> second = new Promise<>();
        registry.register(first);
        registry.register(second);

        registry.close();

        assertTrue(first.await().failed());
        assertEquals(PredefinedErrorCode.SERVICE_UNAVAILABLE, first.await().cause().errorCode());
        assertTrue(second.await().failed());
        assertFalse(registry.active());
        assertEquals(0, registry.size());
    }
}
