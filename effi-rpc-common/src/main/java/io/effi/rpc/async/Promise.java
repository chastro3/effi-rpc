package io.effi.rpc.async;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> extends AbstractFuture<T> {

    public static final Promise<Void> VOID = completed(null);

    public Promise() {
        super(null);
    }

    public Promise(ScheduledExecutorService scheduler) {
        super(scheduler);
    }

    public Promise(ScheduledExecutorService scheduler, Listener<Result<T>> listener) {
        super(scheduler, listener);
    }

    public static <T> Promise<Void> asVoid(Future<T> promise) {
        if (promise.completed() && promise.succeeded()) {
            return VOID;
        }
        Promise<Void> voidPromise = new Promise<>();
        promise.onComplete(result -> {
            if (result.succeeded()) {
                voidPromise.success(null);
            } else {
                voidPromise.failure(result.cause());
            }
        });
        return voidPromise;
    }

    public static <T> Promise<T> completed(T value) {
        return new Promise<T>().success(value);
    }

    public static Promise<Void> completedVoid() {
        return VOID;
    }

    public static Promise<Void> allOf(List<? extends Future<?>> futures) {
        int size = futures.size();
        if (size == 0) return VOID;

        Promise<Void> result = new Promise<>();
        AtomicInteger remaining = new AtomicInteger(size);

        for (Future<?> future : futures) {
            future.onComplete(r -> {
                if (r.failed()) {
                    result.failure(r.cause());
                } else if (remaining.decrementAndGet() == 0) {
                    result.success(null);
                }
            });
        }

        return result;
    }

    public Promise<T> success(T value) {
        return tryComplete(value);
    }

    public Promise<T> failure(Throwable cause) {
        return tryComplete(cause);
    }

    @Override
    public Promise<T> onComplete(Consumer<Result<T>> handler) {
        return (Promise<T>) super.onComplete(handler);
    }

    @Override
    protected Promise<T> tryComplete(Object value) {
        return (Promise<T>) super.tryComplete(value);
    }

    public <R> Promise<R> compose(Function<T, Future<R>> mapper) {
        Promise<R> next = new Promise<>(scheduler);
        onComplete(res -> {
            if (res.succeeded()) {
                Future<R> future = mapper.apply(res.result());
                future.onComplete(r -> {
                    if (r.succeeded()) next.success(r.result());
                    else next.failure(r.cause());
                });
            } else {
                next.failure(res.cause());
            }
        });
        return next;
    }
}

