package io.effi.rpc.engine;

import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Ordered;
import io.effi.rpc.contract.*;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.RegistryConfig;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.filter.Filter;
import io.effi.rpc.contract.filter.FilterChain;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.builder.CallerBuilder;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.transport.TransportSupport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Abstract implementation of {@link Caller}.
 *
 * @param <R> the type of the result
 */
public abstract class AbstractCaller<R> extends AbstractInvoker<CompletableFuture<R>> implements Caller<R> {

    protected EffiRpcModule module;

    protected Locator locator;

    protected ThreadPool threadPool;

    protected CallerModularConfig modularConfig;

    protected AbstractCaller(NodeConfig config, CallerBuilder<?, ?> builder) {
        super(config, builder);
        this.module = AssertUtil.notNull(builder.module(), "module");
        this.locator = AssertUtil.notNull(builder.locator(), "locator");
        this.returnType = builder.returnType();
        this.threadPool = getThreadPool(module, Constant.DEFAULT_CLIENT_HYBRID_THREAD_POOL);
        this.modularConfig = new CallerModularConfig(this.module, builder.clientConfig(), this);
        this.module.register(this);
        addFilter(builder.filters().toArray(Filter[]::new));
        set(KeyConstant.LAST_CALL_INDEX, new AtomicInteger(-1));
        set(CallerMetrics.GENERIC_KEY, new CallerMetrics());
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<R> call(Object... args) throws EffiRpcException {
        return (CompletableFuture<R>) startCall(args, CompletableReplyFuture::new).completableFuture();
    }

    @SuppressWarnings("unchecked")
    @Override
    public R blockingCall(Object... args) throws EffiRpcException {
        return (R) startCall(args, CompletableReplyFuture::new).get();
    }

    @Override
    public CompletableFuture<R> invoke(Object... args) throws EffiRpcException {
        return call(args);
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }
    @Override
    public ClientConfig clientConfig() {
        return modularConfig.clientConfig();
    }

    @Override
    public Locator locator() {
        return locator;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }

    @Override
    public List<RegistryConfig> registryConfigs() {
        return modularConfig.registryConfigs();
    }

    @Override
    public void addFilter(Filter<?, ?, ?>... filters) {
        modularConfig.addFilter(filters);
    }

    @Override
    public <T extends ReplyFuture> T callWithFuture(T future) throws EffiRpcException {
        return TransportSupport.sendRequest(protocol, doCall(future));
    }

    protected <T extends ReplyFuture> T startCall(Object[] args, Function<InvocationContext<Envelope.Request, Caller<?>>, T> futureCreator) {
        Envelope.Request request = protocol.createRequest(this, args);
        InvocationContext<Envelope.Request, Caller<?>> context = new InvocationContext<>(module, request, this, args);
        return callWithFuture(futureCreator.apply(context));
    }

    private <T extends ReplyFuture> T doCall(T future) {
        var context = future.context();
        MetricsSupport.recordStartTime(context);
        // Chain of nested invocations for address resolution and filter execution
        var rpcContext = context.executor(() -> {
            var filterContext = context.executor(() -> {
                InetSocketAddress remoteAddress = locator().locate(context);
                context.source().url().address(remoteAddress);
                var chosenContext = context.executor(() -> {
                    future.whenComplete(replyContext -> {
                        replyContext = replyContext.executor(replyContext::result);
                        FilterChain.execute(replyContext, Ordered.sort(modularConfig.replyFilters()));
                    });
                    return new Result(context.source().url(), future);
                });
                return FilterChain.execute(chosenContext, Ordered.sort(modularConfig.chosenFilters()));
            });
            return FilterChain.execute(filterContext, Ordered.sort(modularConfig.invokeFilters()));
        });
        rpcContext.execute();
        return future;
    }

}
