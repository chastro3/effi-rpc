package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.AssertUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Provides utility operations for composing unary RPC futures.
 */
public final class Futures {

    private Futures() {
    }

    /**
     * Converts a future result to a void future.
     *
     * @param future source future
     * @return future completed with the source completion, carrying {@code null} on success
     */
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

    /**
     * Applies a deadline to a source future.
     *
     * @param source   source future
     * @param deadline completion deadline
     * @return source future when the deadline is disabled; otherwise a deadline-aware future
     */
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

    /**
     * Returns a future completed when every input future succeeds.
     * <p>
     * The returned future completes with the first failure and propagates cancellation to all inputs.
     *
     * @param futures input futures
     * @return aggregate future
     */
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

    /**
     * Returns an already completed void future.
     */
    public static Future<Void> completedVoid() {
        return Promise.completed(null);
    }

    /**
     * Composes a source future with a mapper that returns another future.
     * <p>
     * Cancellation of the returned future propagates to the source or mapped future.
     *
     * @param source source future
     * @param mapper mapper producing the next future
     * @return future completed with the mapped future result
     */
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
}
