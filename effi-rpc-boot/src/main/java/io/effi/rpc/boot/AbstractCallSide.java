package io.effi.rpc.boot;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.CallSideContainer;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.context.ImmutableInterceptorChain;
import io.effi.rpc.base.context.ImmutableStageChain;
import io.effi.rpc.base.context.Interceptor;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.base.context.StageInterceptor;
import io.effi.rpc.boot.builder.CallSideBuilder;
import io.effi.rpc.boot.stage.CallInterceptStage;
import io.effi.rpc.boot.util.CallInterceptorClassifyHandler;
import io.effi.rpc.boot.util.ExecutionUnitClassifier;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigName;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides an abstract implementation of {@link CallSide}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractCallSide<B extends CallSideBuilder> extends AbstractAttributes implements CallSide {

    protected String id;

    protected NodeConfig config;

    protected QueryPath queryPath;

    protected TypeToken<?> returnType;

    protected Protocol protocol;

    protected ThreadPool threadPool;

    protected EffiRpcModule module;

    protected StageChain callStageChain;

    protected InterceptorChain callInterceptorChain;

    protected AbstractCallSide(NodeConfig config, B builder) {
        initialize(config, builder);
        onInitialized(config, builder);
    }

    @Override
    public NodeConfig config() {
        return config;
    }

    @Override
    public QueryPath queryPath() {
        return queryPath;
    }

    protected void initialize(NodeConfig config, B builder) {
        this.config = AssertUtil.notNull(config, "config");
        this.module = AssertUtil.notNull(builder.module(), "module");
        this.queryPath = checkQueryPath(config);
        this.threadPool = checkThreadPool(builder);
        this.returnType = builder.returnType();
        this.protocol = TransportSupport.getProtocol(builder.protocol());
        this.id = CallSideContainer.invokerKey(protocol(), queryPath.path());
    }

    @Override
    public String protocol() {
        return protocol.protocol();
    }

    @Override
    public EffiRpcModule module() {
        return module;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }


    @Override
    public TypeToken<?> replyType() {
        return returnType;
    }

    @Override
    public StageChain callStageChain() {
        return callStageChain;
    }

    @Override
    public InterceptorChain callInterceptorChain() {
        return callInterceptorChain;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return queryPath.toString();
    }

    protected void onInitialized(NodeConfig config, B builder) {
        configureStageChain(builder);
        configureInterceptorChain(builder);
    }

    private QueryPath checkQueryPath(Config config) {
        String path = config.get(DefaultConfigNames.PATH);
        return path == null
                ? QueryPath.empty()
                : QueryPath.valueOf(path);
    }

    private ThreadPool checkThreadPool(B builder) {
        ThreadPool threadPool = builder.threadPool();
        if (threadPool != null) return threadPool;
        ConfigName key = null;
        if (this instanceof Caller<?>) {
            key = DefaultConfigNames.CALLER_THREAD_POOL;
        } else if (this instanceof Callee) {
            key = DefaultConfigNames.CALLEE_THREAD_POOL;
        }
        String name = getConfig(key);
        threadPool = platform().lookup(ThreadPool.class, name);
        AssertUtil.condition(threadPool != null, "No thread pool available for '{}'", id());
        return threadPool;
    }

    protected void configureStageChain(B builder) {
        StageChain callChain = builder.callStageChain();
        if (callChain == null) {
            String[] callStageNames = splitConfig(DefaultConfigNames.CALL_STAGE_CHAIN);
            if (CollectionUtil.isEmpty(callStageNames)) {
                callStageNames = defaultCallStageChain();
            }
            AssertUtil.condition(CollectionUtil.isNotEmpty(callStageNames), "call stage chain cannot be empty");
            callChain = ImmutableStageChain.of(module(), callStageNames);
        }
        this.callStageChain = callChain;
    }

    protected void configureInterceptorChain(B builder) {
        InterceptorChain callChain = builder.callInterceptorChain();
        if (callChain == null) {
            List<String> callNames = new ArrayList<>();
            ExecutionUnitClassifier<Interceptor> filterClassifier = new ExecutionUnitClassifier<>(this, protocol);
            filterClassifier.handler(CallInterceptorClassifyHandler.of((name, filter) -> callNames.add(name)))
                    .classify(lookupConfiguredInterceptors());
            tryAddStageInterceptor(callStageChain, CallInterceptStage.NAME, callNames);
            callChain = ImmutableInterceptorChain.of(module(), StringUtil.toArray(callNames));
        }
        this.callInterceptorChain = callChain;
    }

    protected Map<String, Interceptor> lookupConfiguredInterceptors() {
        Set<String> interceptorNames = CollectionUtil.toLinkedHashSet(splitConfig(DefaultConfigNames.INTERCEPTOR_CHAIN));
        if (CollectionUtil.isEmpty(interceptorNames)) {
            List<String> configuredInterceptors = getCascadedConfig(DefaultConfigNames.INTERCEPTOR);
            interceptorNames = new HashSet<>(configuredInterceptors);
        }
        Set<String> excludedInterceptors = CollectionUtil.toHashSet(splitConfig(DefaultConfigNames.EXCLUDED_INTERCEPTOR));
        Set<String> finalInterceptorNames = interceptorNames;
        return module().extensionMapOf(Interceptor.class, (name, holder) ->
                (finalInterceptorNames.contains(name) || holder.hasTags(Tags.FORCE_ACTIVE))
                        && !excludedInterceptors.contains(name)
        );
    }

    protected void tryAddStageInterceptor(StageChain head, String stageName, List<String> interceptorNames) {
        if (head instanceof ImmutableStageChain headChain) {
            ImmutableStageChain chain = headChain.getChain(stageName);
            if (chain != null && chain.next() != null) {
                StageInterceptor<?> stageInterceptor = StageInterceptor.getInstance(chain.next());
                interceptorNames.add(stageInterceptor.name());
            }
        }
    }

    protected abstract String[] defaultCallStageChain();

}
