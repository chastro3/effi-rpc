package io.effi.rpc.context.support;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerContainer;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.context.Stage;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.TypeCapture;

import java.util.List;

/**
 * Provides an abstract implementation of {@link Peer}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPeer<B extends AbstractPeer.Builder> extends AbstractAttributes implements ConfigurablePeer {

    protected String id;

    protected HierarchicalConfig config;

    protected QueryPath queryPath;

    protected TypeCapture<?> replyType;

    protected Protocol protocol;

    protected ThreadPool threadPool;

    protected ScopedModule module;

    protected Stage.Chain callStageChain;

    protected Stage.Chain replyStageChain;

    protected Interceptor.Chain callInterceptorChain;

    protected Interceptor.Chain replyInterceptorChain;

    protected AbstractPeer(B builder) {
        initialize(builder);
        onInitialized(builder);
    }

    @Override
    public ConfigurablePeer withThreadPool(ThreadPool threadPool) {
        this.threadPool = AssertUtil.notNull(threadPool, "threadPool");
        return this;
    }

    @Override
    public ConfigurablePeer withCallStageChain(Stage.Chain chain) {
        this.callStageChain = AssertUtil.notNull(chain, "chain");
        return this;
    }

    @Override
    public ConfigurablePeer withReplyStageChain(Stage.Chain chain) {
        this.replyStageChain = AssertUtil.notNull(chain, "chain");
        return this;
    }

    @Override
    public ConfigurablePeer withCallInterceptorChain(Interceptor.Chain chain) {
        this.callInterceptorChain = AssertUtil.notNull(chain, "chain");
        return this;
    }

    @Override
    public ConfigurablePeer withReplyInterceptorChain(Interceptor.Chain chain) {
        this.replyInterceptorChain = AssertUtil.notNull(chain, "chain");
        return this;
    }

    @Override
    public HierarchicalConfig config() {
        return config;
    }

    @Override
    public QueryPath queryPath() {
        return queryPath;
    }

    @Override
    public Protocol protocol() {
        return protocol;
    }

    @Override
    public ScopedModule module() {
        return module;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }

    @Override
    public TypeCapture<?> replyType() {
        return replyType;
    }

    @Override
    public Stage.Chain callStageChain() {
        return callStageChain;
    }

    @Override
    public Stage.Chain replyStageChain() {
        return replyStageChain;
    }

    @Override
    public Interceptor.Chain callInterceptorChain() {
        return callInterceptorChain;
    }

    @Override
    public Interceptor.Chain replyInterceptorChain() {
        return replyInterceptorChain;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return queryPath.toString();
    }

    protected void initialize(B builder) {
        this.config = AssertUtil.notNull(builder.config, "config");
        this.module = AssertUtil.notNull(builder.module, "module");
        this.queryPath = findQueryPath();
        this.replyType = builder.replyType;
        this.protocol = platform().namedExtension(Protocol.class, builder.protocol);
        this.id = PeerContainer.invokerKey(protocol().name(), queryPath.path());
    }

    protected void onInitialized(B builder) {
        configureThreadPool(builder);
        configureStageChain(builder);
        configureInterceptorChain(builder);
    }

    private QueryPath findQueryPath() {
        List<String> pathSegments = getMergedConfig(ConfigNames.PATH);
        return CollectionUtil.isEmpty(pathSegments)
                ? QueryPath.empty()
                : QueryPath.valueOf(pathSegments);
    }

    private void configureThreadPool(B builder) {
        ThreadPool threadPool = builder.threadPool;
        if (threadPool != null) withThreadPool(threadPool);
        module.preferredExtension(
                ThreadPoolConfigurator.class,
                getConfig(ConfigNames.THREAD_POOL_CONFIGURATOR)
        ).configure(this);
    }

    protected void configureStageChain(B builder) {
        Stage.Chain callChain = builder.callStageChain;
        if (callChain != null) withCallStageChain(callChain);
        Stage.Chain replyChain = builder.replyStageChain;
        if (replyChain != null) withReplyStageChain(replyChain);
        if (callChain == null || replyChain == null) {
            module.preferredExtension(
                    StageChainConfigurator.class,
                    getConfig(ConfigNames.STAGE_CHAIN_CONFIGURATOR)
            ).configure(this);
        }
    }

    protected void configureInterceptorChain(B builder) {
        Interceptor.Chain callChain = builder.callInterceptorChain;
        if (callChain != null) withCallInterceptorChain(callChain);
        Interceptor.Chain replyChain = builder.replyInterceptorChain;
        if (replyChain != null) withReplyInterceptorChain(replyChain);
        boolean isCaller = this instanceof ConfigurableCaller;
        Interceptor.Chain chosenChain = null;
        if (isCaller) {
            chosenChain = ((AbstractCaller.Builder) builder).chosenInterceptorChain;
            if (chosenChain != null) ((ConfigurableCaller) this).withChosenInterceptorChain(chosenChain);
        }
        if (callChain == null || replyChain == null || (isCaller && chosenChain == null)) {
            module.preferredExtension(
                    InterceptorChainConfigurator.class,
                    getConfig(ConfigNames.INTERCEPTOR_CHAIN_CONFIGURATOR)
            ).configure(this);
        }
    }

    /**
     * Builds {@link Peer} instance and defines configuration.
     */
    protected abstract static class Builder<T extends Peer, SELF extends Builder<T, SELF>>
            implements FluentBuilder<T, SELF> {

        protected String protocol;

        protected HierarchicalConfig config;

        protected PeerContainer<?> container;

        protected ScopedModule module;

        protected TypeCapture<?> replyType;

        protected Stage.Chain callStageChain;

        protected Stage.Chain replyStageChain;

        protected Interceptor.Chain callInterceptorChain;

        protected Interceptor.Chain replyInterceptorChain;

        protected ThreadPool threadPool;

        protected Builder(String protocol, HierarchicalConfig config) {
            this.protocol = AssertUtil.notBlank(protocol, "protocol");
            this.config = config == null
                    ? new DefaultHierarchicalConfig(this)
                    : config;
        }

        public SELF path(String path) {
            config.set(ConfigNames.PATH, path);
            return self();
        }

        public SELF module(ScopedModule module) {
            this.module = module;
            return self();
        }

        public SELF threadPool(ThreadPool threadPool) {
            this.threadPool = threadPool;
            return self();
        }

        public SELF compression(String compression) {
            config.set(ConfigNames.COMPRESSION, compression);
            return self();
        }

        public SELF serialization(String serialization) {
            config.set(ConfigNames.SERIALIZATION, serialization);
            return self();
        }

        public SELF callStageChain(Stage.Chain chain) {
            this.callStageChain = chain;
            return self();
        }

        public SELF replyStageChain(Stage.Chain chain) {
            this.replyStageChain = chain;
            return self();
        }

        public SELF callInterceptorChain(Interceptor.Chain chain) {
            this.callInterceptorChain = chain;
            return self();
        }

        public SELF replyInterceptorChain(Interceptor.Chain chain) {
            this.replyInterceptorChain = chain;
            return self();
        }
    }
}
