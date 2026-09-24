package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.AssertUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Utilities for composing unary RPC futures.
 */
public final class Futures {

    public static Future<Void> completedVoid() {
        return Promise.completed(null);
    }

    public static Future<Void> asVoid(Future<?> future) {
        AssertUtil.notNull(future, "future");
        Promise<Void> result = new Promise<>();
        result.onCancel(future::cancel);
        future.onComplete(completion -> {
            if (completion.failed()) {
                result.failure(completion.cause());
            } else {
                result.success(null);
            }
        });
        return result;
    }

    public static <T> Future<T> withDeadline(Future<T> source, Deadline deadline) {
        AssertUtil.notNull(source, "source");
        AssertUtil.notNull(deadline, "deadline");
        if (deadline.isNone()) return source;
        Promise<T> result = new Promise<>();
        result.onCancel(source::cancel);
        source.onComplete(result::complete);
        CompletableFuture.delayedExecutor(deadline.remainingNanos(), TimeUnit.NANOSECONDS)
                .execute(() -> result.cancel(
                        PredefinedErrorCode.DEADLINE_EXCEEDED.fail(deadline.remainingNanos())
                ));
        return result;
    }

    public static Future<Void> allOf(Collection<? extends Future<?>> futures) {
        AssertUtil.notNull(futures, "futures");
        if (futures.isEmpty()) return completedVoid();
        Promise<Void> result = new Promise<>();
        AtomicInteger remaining = new AtomicInteger(futures.size());
        result.onCancel(reason -> futures.forEach(future -> future.cancel(reason)));
        for (Future<?> future : futures) {
            future.onComplete(completion -> {
                if (completion.failed()) {
                    result.complete(Result.failure(completion.cause()));
                } else if (remaining.decrementAndGet() == 0) {
                    result.success(null);
                }
            });
        }
        return result;
    }

    public static <T, R> Future<R> compose(Future<T> source, Function<? super T, ? extends Future<R>> mapper) {
        AssertUtil.notNull(source, "source");
        AssertUtil.notNull(mapper, "mapper");
        Promise<R> next = new Promise<>();
        AtomicReference<EffiRpcException> cancelReason = new AtomicReference<>();
        AtomicReference<Future<R>> mappedFuture = new AtomicReference<>();
        next.onCancel(reason -> {
            cancelReason.set(reason);
            Future<R> mapped = mappedFuture.get();
            Objects.requireNonNullElse(mapped, source).cancel(reason);
        });
        source.onComplete(result -> {
            if (result.failed()) {
                next.failure(result.cause());
                return;
            }
            Future<R> mapped;
            try {
                mapped = mapper.apply(result.value());
            } catch (Throwable e) {
                next.failure(ConcurrentErrorCodes.FUTURE_MAPPER_FAILED.fail(e, "compose"));
                return;
            }
            if (mapped == null) {
                next.failure(ConcurrentErrorCodes.FUTURE_MAPPER_RETURNED_NULL.fail());
                return;
            }
            mappedFuture.set(mapped);
            if (next.completed()) {
                EffiRpcException reason = cancelReason.get();
                if (reason != null) {
                    mapped.cancel(reason);
                }
                return;
            }
            mapped.onComplete(next::complete);
        });
        return next;
    }

    private Futures() {
    }
}
