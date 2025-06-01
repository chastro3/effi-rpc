package io.effi.rpc.base;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.StringUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * {@link CompletableFuture} implementation of {@link ReplyFuture}.
 */
public class CompletableReplyFuture extends ReplyFuture {

    private final CompletableFuture<Object> completableFuture;

    private final AtomicInteger errorCount = new AtomicInteger(0);

    private FailureHandler failureHandler;

    public CompletableReplyFuture(InvocationContext<Envelope.Request, Caller<?>> context) {
        super(context);
        this.completableFuture = new CompletableFuture<>().whenComplete((v, e) -> remove());
    }

    public CompletableReplyFuture failureHandler(FailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    @Override
    public void startTimeout() {
        String timeoutStr = context.invoker().get(DefaultConfigKeys.TIMEOUT);
        if (StringUtil.isNotBlank(timeoutStr)) {
            completableFuture.orTimeout(Long.parseLong(timeoutStr), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void whenComplete(Consumer<ReplyContext<Envelope.Response, Caller<?>>> consumer) {
        if (errorCount.get() == 0) {
            completedConsumers.add(consumer);
        }
    }

    @Override
    protected void doComplete(Object value) {
        completableFuture.complete(value);
    }

    @Override
    protected void doCompleteExceptionally(EffiRpcException e) {
        if (failureHandler != null) {
            failureHandler.handle(this, e);
        }
    }

    @Override
    public boolean completed() {
        return completableFuture.isDone();
    }

    public Object get() {
        try {
            return completableFuture.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                String timeout = context().invoker().get(DefaultConfigKeys.TIMEOUT);
                throw PredefinedErrorCode.TIMEOUT.fail(e, timeout, id());
            }
            throw PredefinedErrorCode.CALL_CALLER.fail(e.getCause(), toString());
        }
    }

    public AtomicInteger errorCount() {
        return errorCount;
    }

    public CompletableFuture<Object> completableFuture() {
        return completableFuture;
    }

    /**
     * Handles failures by processing {@link EffiRpcException} during RPC execution.
     */
    @FunctionalInterface
    public interface FailureHandler {

        /**
         * Handles the failure when an {@link EffiRpcException} is thrown.
         *
         * @param e the exception encountered during RPC execution
         */
        void handle(CompletableReplyFuture future, EffiRpcException e);
    }
}
