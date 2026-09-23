package io.effi.rpc.context;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.concurrent.AbstractFuture;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import io.effi.rpc.context.options.CallerOptions;

public class ReplyFuture extends AbstractFuture<ReplyContext<Response, Caller<?>>> implements SmartURL.Supplier {

    private static final Logger logger = LoggerFactory.getLogger(ReplyFuture.class);

    private static final Map<Long, ReplyFuture> FUTURES = new ConcurrentHashMap<>();

    private static final AtomicLong INCREASE = new AtomicLong(0);

    private final long id;

    private final CallContext<Request, Caller<?>> context;

    private final AtomicReference<List<Runnable>> cancellationActions = new AtomicReference<>(List.of());

    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);

    private final AtomicBoolean terminal = new AtomicBoolean(false);

    private volatile Interaction.Result rawResult;

    protected ReplyFuture(CallContext<Request, Caller<?>> context) {
        super(findScheduler(context.platform()));
        this.id = INCREASE.incrementAndGet();
        this.context = context;
        context.set(KeyConstant.ATTR_UNIQUE_ID, id);
        context.message().url().set(KeyConstant.ATTR_UNIQUE_ID, id);
        FUTURES.put(id, this);
        Integer timeout = context.peer().option(CallerOptions.TIMEOUT);
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
        return this;
    }

    public ReplyFuture failure(EffiRpcException cause) {
        if (!completed()) {
            tryComplete(cause);
        }
        return this;
    }

    /**
     * Registers a callback invoked only when this call is cancelled by timeout.
     * The callback is executed at most once.
     */
    public ReplyFuture onCancel(Runnable action) {
        AssertUtil.notNull(action, "action");
        if (cancellationRequested.get()) {
            runCancellationAction(action);
            return this;
        }
        if (terminal.get()) {
            return this;
        }
        cancellationActions.updateAndGet(actions -> {
            List<Runnable> updated = new ArrayList<>(actions);
            updated.add(action);
            return List.copyOf(updated);
        });
        if (cancellationRequested.get()) {
            AtomicBoolean removed = new AtomicBoolean(false);
            cancellationActions.updateAndGet(actions -> {
                if (!actions.contains(action)) {
                    return actions;
                }
                List<Runnable> updated = new ArrayList<>(actions);
                updated.remove(action);
                removed.set(true);
                return List.copyOf(updated);
            });
            if (removed.get()) {
                runCancellationAction(action);
            }
        } else if (terminal.get()) {
            cancellationActions.updateAndGet(actions -> removeCancellationAction(actions, action));
        }
        return this;
    }

    @Override
    public SmartURL url() {
        return context.message().url();
    }

    @Override
    public ReplyFuture timeout(long delay, TimeUnit unit) {
        super.timeout(delay, unit);
        return this;
    }

    @Override
    protected Throwable timeoutException(long delay, TimeUnit unit) {
        return InteractionErrorCodes.SERVICE_CALL_TIMEOUT.fail(delay + " " + unit, id);
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
            if (res.failed()) {
                promise.failure(res.cause());
                return;
            }
            Interaction.Result result = rawResult;
            if (result == null) {
                promise.failure(InteractionErrorCodes.REPLY_RESULT_MISSING.fail(id));
                return;
            }
            if (result.succeeded()) {
                promise.success((T) result.result());
            } else {
                promise.failure(result.cause());
            }
        });
        return promise;
    }

    public void withRawResult(Interaction.Result rawResult) {
        this.rawResult = rawResult;
    }

    @Override
    protected void onCompletion(Result<ReplyContext<Response, Caller<?>>> result, boolean timedOut) {
        FUTURES.remove(id);
        if (timedOut) {
            if (cancellationRequested.compareAndSet(false, true)) {
                cancellationActions.getAndSet(List.of()).forEach(this::runCancellationAction);
            }
        } else {
            terminal.set(true);
            cancellationActions.set(List.of());
        }
    }

    private List<Runnable> removeCancellationAction(List<Runnable> actions, Runnable action) {
        if (!actions.contains(action)) {
            return actions;
        }
        List<Runnable> updated = new ArrayList<>(actions);
        updated.remove(action);
        return List.copyOf(updated);
    }

    private void runCancellationAction(Runnable action) {
        if (action == null) {
            return;
        }
        try {
            action.run();
        } catch (Throwable e) {
            logger.error("Failed to cancel reply future '{}'", e, id);
        }
    }
}

