package io.effi.rpc.async;

import io.effi.rpc.executor.RpcThreadFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public abstract class AbstractFuture<T> implements Future<T> {

    private static final LazySingleton<ScheduledExecutorService> DEFAULT_SCHEDULER =
            LazySingleton.from(AbstractFuture::initializeDefaultScheduler);

    protected volatile Result<T> result;

    protected Listener<Result<T>> listener;

    protected final ScheduledExecutorService scheduler;

    private volatile Thread waiter;

    protected AbstractFuture() {
        this(null);
    }

    protected AbstractFuture(ScheduledExecutorService scheduler) {
        this(scheduler, null);
    }

    protected AbstractFuture(ScheduledExecutorService scheduler, Listener<Result<T>> listener) {
        this.scheduler = scheduler;
        this.listener = (listener != null) ? listener : new ArrayListener<>();
    }

    @Override
    public boolean completed() {
        return result != null;
    }

    @Override
    public Future<T> onComplete(Consumer<Result<T>> handler) {
        AssertUtil.notNull(handler, "handler");
        Result<T> r = result;
        if (r != null) {
            handler.accept(r);
            return this;
        }
        synchronized (this) {
            r = result;
            if (r == null) {
                listener.add(handler);
                return this;
            }
        }
        handler.accept(r);
        return this;
    }

    @Override
    public Future<T> timeout(long delay, TimeUnit unit) {
        scheduler().schedule(() -> {
            if (!completed()) {
                tryComplete(new TimeoutException("Promise timed out after " + delay + " " + unit));
            }
        }, delay, unit);
        return this;
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        CompletableFuture<T> cf = new CompletableFuture<>();
        onComplete(res -> {
            if (res.succeeded()) cf.complete(res.result());
            else cf.completeExceptionally(res.cause());
        });
        return cf;
    }


    @Override
    public boolean succeeded() {
        Result<T> r = result;
        return r != null && r.succeeded();
    }

    @Override
    public boolean failed() {
        Result<T> r = result;
        return r != null && r.failed();
    }

    @Override
    public T result() {
        Result<T> r = result;
        return r != null ? r.result() : null;
    }

    @Override
    public Throwable cause() {
        Result<T> r = result;
        return r != null ? r.cause() : null;
    }


    public T await() {
        if (completed()) return result();
        waiter = Thread.currentThread();
        while (!completed()) {
            LockSupport.park(this);
        }
        waiter = null;
        return result();
    }

    public T await(long timeout, TimeUnit unit) {
        if (completed()) return result();

        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        waiter = Thread.currentThread();
        while (!completed()) {
            if (nanos <= 0L) {
                tryComplete(new TimeoutException("Future timed out"));
                break;
            }
            LockSupport.parkNanos(this, nanos);
            nanos = deadline - System.nanoTime();
        }

        waiter = null;
        return result();
    }


    protected ScheduledExecutorService scheduler() {
        return (scheduler != null) ? scheduler : DEFAULT_SCHEDULER.ensure();
    }

    @SuppressWarnings("unchecked")
    protected AbstractFuture<T> tryComplete(Object value) {
        Listener<Result<T>> l;
        Result<T> r;
        synchronized (this) {
            if (result != null) return this;
            r = (value instanceof Throwable)
                    ? Result.failure((Throwable) value)
                    : Result.success((T) value);
            result = r;
            l = listener;
            listener = null;
        }
        if (l != null) {
            l.trigger(r);
            l.clear();
        }
        Thread w = waiter;
        waiter = null;
        if (w != null) {
            LockSupport.unpark(w);
        }
        return this;
    }

    private static ScheduledExecutorService initializeDefaultScheduler() {
        ScheduledThreadPoolExecutor scheduler =
                new ScheduledThreadPoolExecutor(1, new RpcThreadFactory("effi-default-scheduler"));
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
}

