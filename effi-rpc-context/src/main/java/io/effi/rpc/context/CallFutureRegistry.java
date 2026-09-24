package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.trait.Closeable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Platform-scoped registry for in-flight unary calls.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public final class CallFutureRegistry implements Closeable {

    private final AtomicLong sequence = new AtomicLong();

    private final AtomicBoolean active = new AtomicBoolean(true);

    private final ConcurrentMap<Long, Future<?>> calls = new ConcurrentHashMap<>();

    public long nextId() {
        return sequence.incrementAndGet();
    }

    public synchronized long register(Future<?> future) {
        if (!active.get()) {
            throw new IllegalStateException("Call future registry is closed");
        }
        long callId = nextId();
        calls.put(callId, future);
        future.onComplete(result -> remove(callId));
        return callId;
    }

    public Future<?> lookup(long callId) {
        return calls.get(callId);
    }

    public void onTerminate(long callId, Runnable listener) {
        Future<?> future = calls.get(callId);
        if (future == null) {
            listener.run();
            return;
        }
        future.onComplete(result -> listener.run());
    }

    public synchronized Future<?> remove(long callId) {
        return calls.remove(callId);
    }

    public boolean cancel(long callId, EffiRpcException reason) {
        Future<?> future = calls.get(callId);
        return future != null && future.cancel(reason);
    }

    public int size() {
        return calls.size();
    }

    @Override
    public boolean active() {
        return active.get();
    }

    @Override
    public void close() {
        List<Future<?>> pending;
        synchronized (this) {
            if (!active.compareAndSet(true, false)) {
                return;
            }
            pending = List.copyOf(calls.values());
            calls.clear();
        }
        EffiRpcException reason = PredefinedErrorCode.SERVICE_UNAVAILABLE.fail("platform is closing");
        pending.forEach(future -> future.cancel(reason));
    }
}
