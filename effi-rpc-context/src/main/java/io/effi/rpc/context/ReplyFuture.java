package io.effi.rpc.context;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.ConcurrentErrorCodes;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.AssertUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;

/**
 * Protocol-facing future for a unary call.
 */
public class ReplyFuture implements Future<ReplyContext<Response, Caller<?>>>, SmartURL.Supplier {

    private final long id;

    private final CallContext<Request, Caller<?>> context;

    private final Promise<ReplyContext<Response, Caller<?>>> delegate = new Promise<>();

    private volatile Interaction.Result rawResult;

    protected ReplyFuture(CallContext<Request, Caller<?>> context) {
        this.context = AssertUtil.notNull(context, "context");
        CallFutureRegistry registry = context.platform().singleComponent(CallFutureRegistry.class);
        AssertUtil.notNull(registry, "call future registry");
        this.id = registry.register(this);
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.message().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
        timeout(context.peer().option(TIMEOUT), TimeUnit.MILLISECONDS);
    }

    public static ReplyFuture lookup(ScopedPlatform platform, SmartURL url) {
        if (url == null) return null;
        return lookup(platform, url.get(KeyConstant.ATTR_UNIQUE_ID));
    }

    public static ReplyFuture lookup(ScopedPlatform platform, Long callId) {
        if (platform == null || callId == null) {
            return null;
        }
        CallFutureRegistry registry = platform.singleComponent(CallFutureRegistry.class);
        if (registry == null) {
            return null;
        }
        Future<?> future = registry.lookup(callId);
        return future instanceof ReplyFuture replyFuture ? replyFuture : null;
    }

    public ReplyFuture complete(ReplyContext<Response, Caller<?>> replyContext) {
        if (delegate.completed()) {
            return this;
        }
        Interaction.Result result = replyContext.result();
        rawResult = result;
        if (result.succeeded()) {
            delegate.success(replyContext);
        } else {
            delegate.failure(result.cause());
        }
        return this;
    }

    public ReplyFuture failure(EffiRpcException cause) {
        delegate.failure(cause);
        return this;
    }

    public ReplyFuture onCancel(Consumer<EffiRpcException> action) {
        delegate.onCancel(action);
        return this;
    }

    public ReplyFuture timeout(long delay, TimeUnit unit) {
        if (delay < 0) {
            return this;
        }
        Deadline deadline = Deadline.after(delay, unit);
        CompletableFuture.delayedExecutor(deadline.remainingNanos(), TimeUnit.NANOSECONDS)
                .execute(() -> delegate.cancel(PredefinedErrorCode.DEADLINE_EXCEEDED.fail(delay)));
        return this;
    }

    @Override
    public boolean completed() {
        return delegate.completed();
    }

    @Override
    public ReplyFuture onComplete(Consumer<Result<ReplyContext<Response, Caller<?>>>> handler) {
        delegate.onComplete(handler);
        return this;
    }

    @Override
    public ReplyFuture onCompleteAsync(
            Executor executor,
            Consumer<Result<ReplyContext<Response, Caller<?>>>> handler
    ) {
        delegate.onCompleteAsync(executor, handler);
        return this;
    }

    @Override
    public CompletionStage<Result<ReplyContext<Response, Caller<?>>>> completion() {
        return delegate.completion();
    }

    @Override
    public Result<ReplyContext<Response, Caller<?>>> await() throws InterruptedException {
        return delegate.await();
    }

    @Override
    public Result<ReplyContext<Response, Caller<?>>> await(Deadline deadline) throws InterruptedException {
        return delegate.await(deadline);
    }

    @Override
    public boolean cancel(EffiRpcException reason) {
        return delegate.cancel(reason);
    }

    @Override
    public SmartURL url() {
        return context.message().url();
    }

    public EffiRpcException cause() {
        if (!delegate.completed()) {
            return null;
        }
        try {
            return delegate.await().cause();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConcurrentErrorCodes.INTERRUPTED.fail(e, "await");
        }
    }

    public long id() {
        return id;
    }

    public CallContext<Request, Caller<?>> context() {
        return context;
    }

    public void withRawResult(Interaction.Result rawResult) {
        this.rawResult = rawResult;
    }

    @SuppressWarnings("unchecked")
    public <T> Future<T> toResultFuture() {
        return Futures.compose(delegate, ignored -> {
            Interaction.Result result = rawResult;
            if (result == null) {
                return Promise.failed(InteractionErrorCodes.REPLY_RESULT_MISSING.fail(id));
            }
            if (result.failed()) {
                return Promise.failed(result.cause());
            }
            return Promise.completed((T) result.value());
        });
    }
}
