package io.effi.rpc.context;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.concurrent.ConcurrentErrorCodes;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Provides the protocol-facing future for a unary call.
 */
public class ReplyFuture implements Future<ReplyContext<Response, Caller<?>>>, SmartURL.Supplier {

    private final long id;

    private final CallContext<Request, Caller<?>> context;

    private final Promise<ReplyContext<Response, Caller<?>>> delegate = new Promise<>();

    private volatile Interaction.Result rawResult;

    public ReplyFuture(CallContext<Request, Caller<?>> context) {
        this.context = AssertUtil.notNull(context, "context");
        CallFutureRegistry registry = context.platform().singleComponent(CallFutureRegistry.class);
        AssertUtil.notNull(registry, "call future registry");
        this.id = registry.register(this);
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.message().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
    }

    /**
     * Looks up the reply future registered for the supplied URL.
     *
     * @param platform owning platform
     * @param url      request URL
     * @return matching reply future, or {@code null} when absent
     */
    public static ReplyFuture lookup(ScopedPlatform platform, SmartURL url) {
        if (url == null) return null;
        return lookup(platform, url.get(KeyConstant.ATTR_UNIQUE_ID));
    }

    /**
     * Looks up the reply future registered for the supplied call identifier.
     *
     * @param platform owning platform
     * @param callId   call identifier
     * @return matching reply future, or {@code null} when absent
     */
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

    /**
     * Completes this future with the supplied reply context.
     *
     * @param replyContext reply context
     * @return this future
     */
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

    /**
     * Fails this future with the supplied cause.
     *
     * @param cause failure cause
     * @return this future
     */
    public ReplyFuture failure(EffiRpcException cause) {
        delegate.failure(cause);
        return this;
    }

    /**
     * Registers a cancellation listener.
     *
     * @param action cancellation action
     * @return this future
     */
    public ReplyFuture onCancel(Consumer<EffiRpcException> action) {
        delegate.onCancel(action);
        return this;
    }

    @Override
    public boolean completed() {
        return delegate.completed();
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
    public ReplyFuture onComplete(Consumer<Result<ReplyContext<Response, Caller<?>>>> handler) {
        delegate.onComplete(handler);
        return this;
    }

    @Override
    public SmartURL url() {
        return context.message().url();
    }

    /**
     * Returns the failure cause when this future has completed.
     */
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

    /**
     * Returns the call identifier.
     */
    public long id() {
        return id;
    }

    /**
     * Returns the call context associated with this future.
     */
    public CallContext<Request, Caller<?>> context() {
        return context;
    }

    /**
     * Sets the raw interaction result.
     *
     * @param rawResult raw interaction result
     */
    public void withRawResult(Interaction.Result rawResult) {
        this.rawResult = rawResult;
    }

    /**
     * Returns the raw interaction result.
     */
    public Interaction.Result rawResult() {
        return rawResult;
    }
}
