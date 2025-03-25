package io.effi.rpc.contract;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.extension.spi.ExtensionLoader;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.contract.faulttolerance.FaultTolerance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * {@link CompletableFuture} implementation of {@link ReplyFuture}.
 */
public class CompletableReplyFuture extends ReplyFuture {

    private final CompletableFuture<Object> completableFuture;

    private final AtomicInteger errorCount = new AtomicInteger(0);

    public CompletableReplyFuture(InvocationContext<Envelope.Request, Caller<?>> context) {
        super(context);
        this.completableFuture = new CompletableFuture<>();
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
        FaultTolerance faultTolerance = ExtensionLoader.loadExtension(FaultTolerance.class, context.invoker().config());
        try {
            faultTolerance.operation(this, e);
        } catch (EffiRpcException finalE) {
            completableFuture().completeExceptionally(finalE);
        }
    }

    /**
     * Returns error count of this future.
     */
    public AtomicInteger errorCount() {
        return errorCount;
    }

    /**
     * Returns the completable future of this future.
     */
    public CompletableFuture<Object> completableFuture() {
        return completableFuture;
    }
}
