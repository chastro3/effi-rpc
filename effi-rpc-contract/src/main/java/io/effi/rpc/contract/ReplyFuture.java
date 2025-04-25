package io.effi.rpc.contract;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;

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

    /**
     * Attribute key for associating ReplyFuture with a channel.
     */
    public static final GenericKey<ReplyFuture> ATTRIBUTE_KEY = GenericKey.valueOf("replyFuture");

    /**
     * Map storing all ReplyFutures by their unique IDs.
     */
    public static final Map<Long, ReplyFuture> FUTURES = new ConcurrentHashMap<>();

    /**
     * Atomic counter for generating unique IDs for futures.
     */
    private static final AtomicLong INCREASE = new AtomicLong(0);

    /**
     * List of consumers to be invoked when the future is completed.
     */
    protected final List<Consumer<ReplyContext<Envelope.Response, Caller<?>>>> completedConsumers = new ArrayList<>();

    /**
     * Unique identifier for this future.
     */
    protected final long id;

    /**
     * InvocationContext associated with this future.
     */
    protected InvocationContext<Envelope.Request, Caller<?>> context;

    protected ReplyFuture(InvocationContext<Envelope.Request, Caller<?>> context) {
        this.id = INCREASE.incrementAndGet();
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.source().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
        this.context = context;
        FUTURES.put(id, this);
    }

    /**
     * Retrieves a ReplyFuture by specified URL.
     *
     * @param url the config
     * @return the corresponding ReplyFuture, or null if not found.
     */
    public static ReplyFuture getFuture(URL url) {
        Long id = url.get(KeyConstant.ATTR_UNIQUE_ID);
        return id == null ? null : getFuture(id);
    }

    /**
     * Retrieves a ReplyFuture by its unique ID.
     *
     * @param id the unique ID.
     * @return the corresponding ReplyFuture, or null if not found.
     */
    public static ReplyFuture getFuture(long id) {
        return FUTURES.get(id);
    }

    public static void removeFuture(InvocationContext<?, Caller<?>> context) {
        Long id = context.get(KeyConstant.ATTR_UNIQUE_ID);
        if (id != null) removeFuture(id);
    }

    /**
     * Removes a ReplyFuture by its unique ID.
     *
     * @param id the unique ID.
     */
    public static void removeFuture(long id) {
        FUTURES.remove(id);
    }

    /**
     * Returns the unique ID of this future.
     */
    public long id() {
        return id;
    }

    /**
     * Completes the future exceptionally with the given throwable.
     *
     * @param e the throwable to complete the future with.
     */
    public void complete(EffiRpcException e) {
        doCompleteExceptionally(e);
        removeFuture(id);
    }

    /**
     * Completes the future with the given result, invoking completion consumers.
     *
     * @param context the context to complete the future with.
     */
    public void complete(ReplyContext<Envelope.Response, Caller<?>> context) {
        invokeCompletedConsumers(context);
        Result result = context.result();
        if (result.hasException()) {
            doCompleteExceptionally(result.exception());
        } else {
            doComplete(result.value());
        }
        removeFuture(id);
    }

    /**
     * Removes this future from the FUTURES map.
     */
    public void remove() {
        removeFuture(id);
    }

    /**
     * Registers a consumer to be invoked when the future is completed.
     *
     * @param consumer the consumer to register.
     */
    public abstract void whenComplete(Consumer<ReplyContext<Envelope.Response, Caller<?>>> consumer);

    /**
     * Completes the future with the given value.
     *
     * @param value the result value.
     */
    protected abstract void doComplete(Object value);

    /**
     * Completes the future exceptionally with the given throwable.
     *
     * @param e the exception.
     */
    protected abstract void doCompleteExceptionally(EffiRpcException e);

    /**
     * Invokes all registered completion consumers with the given result.
     *
     * @param context the result to pass to the consumers.
     */
    private void invokeCompletedConsumers(ReplyContext<Envelope.Response, Caller<?>> context) {
        completedConsumers.forEach(consumer -> consumer.accept(context));
    }

    /**
     * Retrieves the InvocationContext associated with this future.
     */
    public InvocationContext<Envelope.Request, Caller<?>> context() {
        return context;
    }

    /**
     * Checks if the future is completed.
     */
    public boolean completed() {
        return false;
    }
}


