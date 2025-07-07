package io.effi.rpc.boot.builder;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.CallSideContainer;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.TypeToken;

/**
 * Builds {@link CallSide} instance and defines configuration.
 */
public abstract class CallSideBuilder<T extends CallSide, C extends CallSideBuilder<T, C>>
        implements FluentBuilder<T, C>, Config.Provider {

    protected NodeConfig config;

    protected CallSideContainer<?> container;

    protected EffiRpcModule module;

    protected TypeToken<?> replyType;

    protected StageChain callStageChain;

    protected InterceptorChain callInterceptorChain;

    protected ThreadPool threadPool;

    protected CallSideBuilder(NodeConfig config) {
        this.config = AssertUtil.notNull(config, "config");
    }

    public C callStageChain(StageChain chain) {
        this.callStageChain = chain;
        return returnThis();
    }

    public C callInterceptorChain(InterceptorChain chain) {
        this.callInterceptorChain = chain;
        return returnThis();
    }


    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
    }

    public C threadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return returnThis();
    }

    public C compression(String compression) {
        config.set(DefaultConfigNames.COMPRESSION, compression);
        return returnThis();
    }

    public C serialization(String serialization) {
        config.set(DefaultConfigNames.SERIALIZATION, serialization);
        return returnThis();
    }

    public C path(String path) {
        config.set(DefaultConfigNames.PATH, path);
        return returnThis();
    }

    public TypeToken<?> returnType() {
        return replyType;
    }

    public StageChain callStageChain() {
        return callStageChain;
    }

    public EffiRpcModule module() {
        return module;
    }

    public ThreadPool threadPool() {
        return threadPool;
    }

    public InterceptorChain callInterceptorChain() {
        return callInterceptorChain;
    }

    protected HierarchicalNodeConfig getNodeConfig(NodeConfig config) {
        if (config instanceof HierarchicalNodeConfig nodeConfig) {
            return nodeConfig;
        } else {
            CallSideContainer<?> container = container();
            NodeConfig parentConfig = container == null ? null : container.config();
            HierarchicalNodeConfig nodeConfig = new HierarchicalNodeConfig(null, parentConfig);
            nodeConfig.set(config.items());
            return nodeConfig;
        }
    }

    @Override
    public NodeConfig config() {
        return config;
    }

    @Override
    public T build() {
        HierarchicalNodeConfig nodeConfig = getNodeConfig(config);
        T instance = build(nodeConfig);
        nodeConfig.setOwner(instance);
        return instance;
    }

    public CallSideContainer<?> container() {
        return container;
    }

    public abstract String protocol();

    protected abstract T build(NodeConfig config);
}

