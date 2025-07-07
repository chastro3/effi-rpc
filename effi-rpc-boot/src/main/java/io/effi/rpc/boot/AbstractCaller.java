package io.effi.rpc.boot;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.CompletableReplyFuture;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.ImmutableInterceptorChain;
import io.effi.rpc.base.context.ImmutableStageChain;
import io.effi.rpc.base.context.Interceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.boot.builder.CallerBuilder;
import io.effi.rpc.boot.stage.CallInterceptStage;
import io.effi.rpc.boot.stage.ChosenInterceptStage;
import io.effi.rpc.boot.stage.FutureResultStage;
import io.effi.rpc.boot.stage.LocatorStage;
import io.effi.rpc.boot.stage.ReplyInterceptStage;
import io.effi.rpc.boot.stage.ReplyResultStage;
import io.effi.rpc.boot.util.CallInterceptorClassifyHandler;
import io.effi.rpc.boot.util.ChosenInterceptorClassifyHandler;
import io.effi.rpc.boot.util.ExecutionUnitClassifier;
import io.effi.rpc.boot.util.ReplyInterceptorClassifyHandler;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.governance.faulttolerance.DefaultFailureHandler;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.metrics.MetricsSupport;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Provides an abstract implementation of {@link Caller}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractCaller<R> extends AbstractCallSide<CallerBuilder> implements Caller<R> {

    private static final String[] DEFAULT_CALL_STAGE_CHAIN = new String[]{
            CallInterceptStage.NAME, LocatorStage.NAME, ChosenInterceptStage.NAME, FutureResultStage.NAME
    };

    private static final String[] DEFAULT_REPLY_STAGE_CHAIN = new String[]{
            ReplyInterceptStage.NAME, ReplyResultStage.NAME
    };

    protected Locator locator;

    protected ClientConfig clientConfig;

    protected InterceptorChain chosenInterceptorChain;

    protected StageChain replyStageChain;

    protected InterceptorChain replyInterceptorChain;

    protected AbstractCaller(NodeConfig config, CallerBuilder builder) {
        super(config, builder);
    }

    @Override
    public CompletableFuture<R> call(Object... args) throws EffiRpcException {
        return (CompletableFuture<R>) startCall(args, CompletableReplyFuture::new)
                .failureHandler(DefaultFailureHandler.getInstance())
                .completableFuture();
    }

    @Override
    public R blockingCall(Object... args) throws EffiRpcException {
        return (R) startCall(args, CompletableReplyFuture::new)
                .failureHandler(DefaultFailureHandler.getInstance())
                .get();
    }

    @Override
    public ClientConfig clientConfig() {
        return clientConfig;
    }

    @Override
    public Locator locator() {
        return locator;
    }

    @Override
    public InterceptorChain chosenInterceptorChain() {
        return chosenInterceptorChain;
    }

    @Override
    public StageChain replyStageChain() {
        return replyStageChain;
    }

    @Override
    public InterceptorChain replyInterceptorChain() {
        return replyInterceptorChain;
    }

    @Override
    public <T extends ReplyFuture> T callWithFuture(T future) throws EffiRpcException {
        return TransportSupport.sendRequest(protocol, doCall(future));
    }

    @Override
    protected void initialize(NodeConfig config, CallerBuilder builder) {
        super.initialize(config, builder);
        this.returnType = builder.returnType();
        this.locator = checkLocator(builder);
        this.clientConfig = checkClientConfig(builder);
    }

    @Override
    protected void onInitialized(NodeConfig config, CallerBuilder builder) {
        super.onInitialized(config, builder);
        module.register(Caller.class, this);
        set(KeyConstant.LAST_CALL_INDEX, new AtomicInteger(-1));
        set(CallerMetrics.GENERIC_KEY, new CallerMetrics());
        tryPreloadDiscoveries();
    }

    @Override
    protected void configureStageChain(CallerBuilder builder) {
        StageChain callChain = builder.callStageChain();
        StageChain replyChain = builder.replyStageChain();
        if (callChain == null) {
            String[] callStageChain = splitConfig(DefaultConfigNames.CALL_STAGE_CHAIN);
            if (CollectionUtil.isEmpty(callStageChain)) {
                callStageChain = defaultCallStageChain();
            }
            AssertUtil.condition(CollectionUtil.isNotEmpty(callStageChain), "call stage chain cannot be empty");
            callChain = ImmutableStageChain.of(module(), callStageChain);
        }
        if (replyChain == null) {
            String[] replyStageChain = splitConfig(DefaultConfigNames.REPLY_STAGE_CHAIN);
            if (CollectionUtil.isEmpty(replyStageChain)) {
                replyStageChain = defaultReplyStageChain();
            }
            AssertUtil.condition(CollectionUtil.isNotEmpty(replyStageChain), "reply stage chain cannot be empty");
            replyChain = ImmutableStageChain.of(module(), replyStageChain);
        }
        this.callStageChain = callChain;
        this.replyStageChain = replyChain;
    }

    @Override
    protected void configureInterceptorChain(CallerBuilder builder) {
        InterceptorChain callChain = builder.callInterceptorChain();
        InterceptorChain chosenChain = builder.chosenInterceptorChain();
        InterceptorChain replyChain = builder.replyInterceptorChain();
        if (callChain == null || chosenChain == null || replyChain == null) {
            List<String> callNames = callChain == null ? new ArrayList<>() : null;
            List<String> chosenNames = chosenChain == null ? new ArrayList<>() : null;
            List<String> replyNames = replyChain == null ? new ArrayList<>() : null;
            ExecutionUnitClassifier<Interceptor> filterClassifier = new ExecutionUnitClassifier<>(this, protocol);
            if (callChain == null)
                filterClassifier.handler(CallInterceptorClassifyHandler.of((name, filter) -> callNames.add(name)));
            if (chosenChain == null)
                filterClassifier.handler(ChosenInterceptorClassifyHandler.of((name, filter) -> chosenNames.add(name)));
            if (replyChain == null)
                filterClassifier.handler(ReplyInterceptorClassifyHandler.of((name, filter) -> replyNames.add(name)));
            filterClassifier.classify(lookupConfiguredInterceptors());
            if (callChain == null) {
                tryAddStageInterceptor(callStageChain, CallInterceptStage.NAME, callNames);
                callChain = ImmutableInterceptorChain.of(module(), StringUtil.toArray(callNames));
            }
            if (chosenChain == null) {
                tryAddStageInterceptor(callStageChain, ChosenInterceptStage.NAME, chosenNames);
                chosenChain = ImmutableInterceptorChain.of(module(), StringUtil.toArray(chosenNames));
            }
            if (replyChain == null) {
                tryAddStageInterceptor(replyStageChain, ReplyInterceptStage.NAME, replyNames);
                replyChain = ImmutableInterceptorChain.of(module(), StringUtil.toArray(replyNames));
            }
        }
        this.callInterceptorChain = callChain;
        this.chosenInterceptorChain = chosenChain;
        this.replyInterceptorChain = replyChain;
    }

    @Override
    protected String[] defaultCallStageChain() {
        return DEFAULT_CALL_STAGE_CHAIN;
    }
    protected String[] defaultReplyStageChain() {
        return DEFAULT_REPLY_STAGE_CHAIN;
    }

    private Locator checkLocator(CallerBuilder builder) {
        Locator locator = builder.locator();
        if (locator != null) return locator;
        String address = config.get(DefaultConfigNames.ADDRESS);
        if (StringUtil.isNotBlank(address)) {
            locator = DirectLocator.of(address);
        } else {
            String remoteApplication = config.get(DefaultConfigNames.REMOTE_APPLICATION);
            if (StringUtil.isNotBlank(remoteApplication)) {
                locator = new RegistryLocator(this, remoteApplication, builder.registryConfigs());
            }
        }
        AssertUtil.condition(locator != null, "No locator available for '{}'", id());
        return locator;
    }

    private ClientConfig checkClientConfig(CallerBuilder builder) {
        ClientConfig clientConfig = builder.clientConfig();
        if (clientConfig != null) return clientConfig;
        String clientConfigName = getConfig(DefaultConfigNames.CLIENT_CONFIG);
        String defaultClientConfigName = ClientConfig.defaultKey(protocol());
        if (StringUtil.isBlank(clientConfigName)) {
            clientConfig = platform().lookup(ClientConfig.class, defaultClientConfigName);
        } else {
            clientConfig = platform().lookup(ClientConfig.class, clientConfigName);
            if (clientConfig == null) {
                clientConfig = platform().lookup(ClientConfig.class, defaultClientConfigName);
            }
        }
        if (clientConfig == null) {
            throw new IllegalStateException("No client config available for '" + id() + "'");
        }
        return clientConfig;
    }

    protected <T extends ReplyFuture> T startCall(Object[] args, Function<CallContext<Message.Request, Caller<?>>, T> futureCreator) {
        Message.Request request = protocol.createRequest(this, args);
        CallContext<Message.Request, Caller<?>> context = new CallContext<>(module, request, this, args);
        return callWithFuture(futureCreator.apply(context));
    }

    private <T extends ReplyFuture> T doCall(T future) {
        var context = future.context();
        MetricsSupport.recordStartTime(context);
        callStageChain().proceed(context);
        return future;
    }

    private void tryPreloadDiscoveries() {
        if (locator instanceof RegistryLocator registryLocator) {
            registryLocator.preloadDiscoveries();
        }
    }

}
