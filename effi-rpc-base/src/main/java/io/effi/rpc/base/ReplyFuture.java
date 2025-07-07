package io.effi.rpc.base;

import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Future for asynchronous replies.
 */
public abstract class ReplyFuture {

    private static final Map<Long, ReplyFuture> FUTURES = new ConcurrentHashMap<>();

    private static final AtomicLong INCREASE = new AtomicLong(0);

    protected final List<Consumer<ReplyContext<Message.Response, Caller<?>>>> completedConsumers = new ArrayList<>();

    protected final long id;

    protected CallContext<Message.Request, Caller<?>> context;

    protected ReplyFuture(CallContext<Message.Request, Caller<?>> context) {
        this.id = INCREASE.incrementAndGet();
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.message().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
        this.context = context;
        FUTURES.put(id, this);
    }

    public static ReplyFuture getFuture(URL url) {
        Long id = url.get(KeyConstant.ATTR_UNIQUE_ID);
        return id == null ? null : getFuture(id);
    }

    public static ReplyFuture getFuture(long id) {
        return FUTURES.get(id);
    }

    public static void removeFuture(long id) {
        FUTURES.remove(id);
    }

    public long id() {
        return id;
    }

    /**
     * Completes the future exceptionally with the given throwable.
     */
    public void complete(EffiRpcException e) {
        if (!completed()) {
            doCompleteExceptionally(e);
        }
    }

    /**
     * Completes the future with the given result, invoking completion consumers.
     *
     * @param context the context to complete the future with.
     */
    public void complete(ReplyContext<Message.Response, Caller<?>> context) {
        if (!completed()) {
            invokeCompletedConsumers(context);
            Result result = context.result();
            if (result.hasException()) {
                doCompleteExceptionally(result.as(ResultType.EXCEPTION));
            } else {
                doComplete(result.value());
            }
        }
    }

    public void remove() {
        removeFuture(id);
    }

    private void invokeCompletedConsumers(ReplyContext<Message.Response, Caller<?>> context) {
        completedConsumers.forEach(consumer -> consumer.accept(context));
    }

    public CallContext<Message.Request, Caller<?>> context() {
        return context;
    }

    /**
     * Checks if the future is completed.
     */
    public abstract boolean completed();

    public abstract void startTimeout();

    protected abstract void doComplete(Object value);

    protected abstract void doCompleteExceptionally(EffiRpcException e);

    public void whenComplete(Consumer<ReplyContext<Message.Response, Caller<?>>> consumer) {
        completedConsumers.add(consumer);
    }
}


