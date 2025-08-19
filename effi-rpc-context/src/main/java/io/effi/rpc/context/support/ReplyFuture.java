package io.effi.rpc.context.support;

import io.effi.rpc.async.AbstractFuture;
import io.effi.rpc.async.Future;
import io.effi.rpc.async.Promise;
import io.effi.rpc.async.Result;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ReplyFuture extends AbstractFuture<ReplyContext<Response, Caller<?>>> implements SmartURL.Supplier {

    private static final Map<Long, ReplyFuture> FUTURES = new ConcurrentHashMap<>();

    private static final AtomicLong INCREASE = new AtomicLong(0);

    private final long id;

    private final CallContext<Request, Caller<?>> context;

    private volatile Interaction.Result rawResult;

    protected ReplyFuture(CallContext<Request, Caller<?>> context) {
        super(findScheduler(context.platform()));
        this.id = INCREASE.incrementAndGet();
        this.context = context;
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.message().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
        FUTURES.put(id, this);
        Integer timeout = context.peer().getConfig(ConfigNames.TIMEOUT);
        timeout(timeout, TimeUnit.MILLISECONDS);
    }

    public static ReplyFuture lookup(SmartURL url) {
        Long id = url.get(KeyConstant.ATTR_UNIQUE_ID);
        return id == null ? null : FUTURES.get(id);
    }

    public static ReplyFuture lookup(long id) {
        return FUTURES.get(id);
    }

    private static ScheduledExecutorService findScheduler(ScopedPlatform platform) {
        Scheduler scheduler = platform.singleComponent(Scheduler.class);
        return scheduler.disposableService();
    }

    public ReplyFuture complete(ReplyContext<Response, Caller<?>> replyContext) {
        if (completed()) return this;
        Interaction.Result result = replyContext.result();
        rawResult = result;
        if (result.succeeded()) tryComplete(replyContext);
        else tryComplete(result.cause());
        FUTURES.remove(id);
        return this;
    }

    public ReplyFuture failure(EffiRpcException cause) {
        if (!completed()) {
            tryComplete(cause);
            FUTURES.remove(id);
        }
        return this;
    }

    @Override
    public SmartURL url() {
        return context.message().url();
    }

    @Override
    public ReplyFuture timeout(long delay, TimeUnit unit) {
        scheduler().schedule(() -> {
            if (!completed()) {
                failure(PredefinedErrorCode.TIMEOUT.fail(null, delay + " " + unit, id));
            }
        }, delay, unit);
        return this;
    }

    @Override
    public ReplyFuture onComplete(Consumer<Result<ReplyContext<Response, Caller<?>>>> handler) {
        return (ReplyFuture) super.onComplete(handler);
    }

    @Override
    public EffiRpcException cause() {
        return (EffiRpcException) super.cause();
    }

    public long id() {
        return id;
    }

    public CallContext<Request, Caller<?>> context() {
        return context;
    }

    @SuppressWarnings("unchecked")
    public <T> Future<T> toResultFuture() {
        Promise<T> promise = new Promise<>();
        onComplete(res -> {
            if (res.succeeded()) promise.success((T) rawResult.result());
            else promise.failure(res.cause());
        });
        return promise;
    }

    public void withRawResult(Interaction.Result rawResult) {
        this.rawResult = rawResult;
    }
}

