package io.effi.rpc.concurrent;

import io.effi.rpc.executor.RpcThreadFactory;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public abstract class AbstractFuture<T> implements Future<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFuture.class);

    private static final LazySingleton<ScheduledExecutorService> DEFAULT_SCHEDULER =
            LazySingleton.from(AbstractFuture::initializeDefaultScheduler);

    protected volatile Result<T> result;

    protected Listener<Result<T>> listener;

    protected final ScheduledExecutorService scheduler;

    private final Set<Thread> waiters = ConcurrentHashMap.newKeySet();

    private volatile ScheduledFuture<?> timeoutFuture;

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
        ScheduledFuture<?> scheduled = scheduler().schedule(() ->
                tryComplete(timeoutException(delay, unit), true),
                delay,
                unit
        );
        synchronized (this) {
            if (result != null) {
                scheduled.cancel(false);
            } else {
                ScheduledFuture<?> previous = timeoutFuture;
                timeoutFuture = scheduled;
                if (previous != null) previous.cancel(false);
            }
        }
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
        Thread current = Thread.currentThread();
        waiters.add(current);
        try {
            while (!completed()) {
                LockSupport.park(this);
            }
        } finally {
            waiters.remove(current);
        }
        return result();
    }

    public T await(long timeout, TimeUnit unit) {
        if (completed()) return result();
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        Thread current = Thread.currentThread();
        waiters.add(current);
        try {
            while (!completed()) {
                if (nanos <= 0L) {
                    tryComplete(timeoutException(timeout, unit), true);
                    break;
                }
                LockSupport.parkNanos(this, nanos);
                nanos = deadline - System.nanoTime();
            }
        } finally {
            waiters.remove(current);
        }
        return result();
    }

    protected ScheduledExecutorService scheduler() {
        return (scheduler != null) ? scheduler : DEFAULT_SCHEDULER.ensure();
    }

    protected Throwable timeoutException(long delay, TimeUnit unit) {
        return new TimeoutException("Promise timed out after " + delay + " " + unit);
    }

    @SuppressWarnings("unchecked")
    protected AbstractFuture<T> tryComplete(Object value) {
        tryComplete(value, false);
        return this;
    }

    private void tryComplete(Object value, boolean timedOut) {
        Listener<Result<T>> l;
        Result<T> r;
        ScheduledFuture<?> timeoutTask;
        synchronized (this) {
            if (result != null) return;
            r = (value instanceof Throwable)
                    ? Result.failure((Throwable) value)
                    : Result.success((T) value);
            result = r;
            l = listener;
            listener = null;
            timeoutTask = timeoutFuture;
            timeoutFuture = null;
        }
        if (timeoutTask != null) timeoutTask.cancel(false);
        try {
            onCompletion(r, timedOut);
        } catch (Throwable e) {
            logger.error("Failed to handle future completion.", e);
        }
        if (l != null) {
            try {
                l.trigger(r);
            } catch (Throwable e) {
                logger.error("Failed to notify future listener.", e);
            } finally {
                l.clear();
            }
        }
        waiters.forEach(LockSupport::unpark);
    }

    /**
     * Invoked exactly once after this future transitions to a terminal state.
     *
     * @param result the terminal result
     * @param timedOut whether the terminal state was caused by timeout
     */
    protected void onCompletion(Result<T> result, boolean timedOut) {
    }

    private static ScheduledExecutorService initializeDefaultScheduler() {
        ScheduledThreadPoolExecutor scheduler =
                new ScheduledThreadPoolExecutor(1, new RpcThreadFactory("effi-default-scheduler"));
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }
}

