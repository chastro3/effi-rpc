package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromiseTest {

    @Test
    void callbackReceivesTerminalResultExactlyOnce() {
        Promise<String> promise = new Promise<>();
        AtomicReference<Result<String>> observed = new AtomicReference<>();
        promise.onComplete(observed::set);

        assertTrue(promise.success("ok"));
        assertFalse(promise.failure(PredefinedErrorCode.COMMON.fail("late")));
        assertEquals("ok", observed.get().value());
    }

    @Test
    void cancellationRunsBeforeCompletionCallback() throws Exception {
        Promise<String> promise = new Promise<>();
        AtomicBoolean cancellationHandled = new AtomicBoolean();
        AtomicBoolean callbackSawCancellation = new AtomicBoolean();
        promise.onCancel(reason -> cancellationHandled.set(true));
        promise.onComplete(result -> callbackSawCancellation.set(cancellationHandled.get()));

        EffiRpcException reason = PredefinedErrorCode.CALL_CANCELLED.fail("client closed");
        assertTrue(promise.cancel(reason));

        assertTrue(cancellationHandled.get());
        assertTrue(callbackSawCancellation.get());
        assertSame(reason, promise.await().cause());
    }

    @Test
    void asyncCallbackUsesProvidedExecutor() throws Exception {
        Promise<String> promise = new Promise<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<String> threadName = new AtomicReference<>();
        try {
            promise.onCompleteAsync(executor, result -> threadName.set(Thread.currentThread().getName()));
            promise.success("ok");
            executor.shutdown();
            assertTrue(executor.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS));
            assertTrue(threadName.get().startsWith("pool-"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void deadlineAwaitCancelsPendingPromise() throws Exception {
        Promise<String> promise = new Promise<>();
        AtomicBoolean cancellationHandled = new AtomicBoolean();
        promise.onCancel(reason -> cancellationHandled.set(true));

        Result<String> result = promise.await(Deadline.after(10, java.util.concurrent.TimeUnit.MILLISECONDS));

        assertTrue(result.failed());
        assertEquals(PredefinedErrorCode.DEADLINE_EXCEEDED, result.cause().errorCode());
        assertTrue(cancellationHandled.get());
    }
}
