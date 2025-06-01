package io.effi.rpc.boot.builder;

import io.effi.rpc.base.Invoker;
import io.effi.rpc.base.InvokerContainer;
import io.effi.rpc.base.ThreadPool;
import io.effi.rpc.base.filter.Filter;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.ConfigSource;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds {@link Invoker} instance and defines configuration.
 */
public abstract class InvokerBuilder<T extends Invoker<?>, C extends InvokerBuilder<T, C>>
        implements FluentBuilder<T, C>, ConfigSource {

    protected NodeConfig config;

    protected InvokerContainer<?> container;

    protected List<Filter<?, ?, ?>> filters = new ArrayList<>();

    protected EffiRpcModule module;

    protected TypeToken<?> returnType;

    protected ThreadPool threadPool;

    protected InvokerBuilder(NodeConfig config) {
        this.config = AssertUtil.notNull(config, "config");
    }

    /**
     * Sets the module.
     */
    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
    }

    /**
     * Sets the thread pool.
     */
    public C threadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
        return returnThis();
    }

    /**
     * Sets the compression type.
     */
    public C compression(String compression) {
        config.set(DefaultConfigKeys.COMPRESSION, compression);
        return returnThis();
    }

    /**
     * Sets the serialization type.
     */
    public C serialization(String serialization) {
        config.set(DefaultConfigKeys.SERIALIZATION, serialization);
        return returnThis();
    }

    /**
     * Sets the query path for the invoker.
     */
    public C path(String path) {
        config.set(DefaultConfigKeys.PATH, path);
        return returnThis();
    }

    /**
     * Adds filters to the invoker.
     */
    public C addFilters(Collection<Filter<?, ?, ?>> filters) {
        CollectionUtil.addUnique(this.filters, filters);
        return returnThis();
    }

    public InvokerContainer<?> container() {
        return container;
    }

    public TypeToken<?> returnType() {
        return returnType;
    }

    public List<Filter<?, ?, ?>> filters() {
        return filters;
    }

    public EffiRpcModule module() {
        return module;
    }

    public ThreadPool threadPool() {
        return threadPool;
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

    protected HierarchicalNodeConfig getNodeConfig(NodeConfig config) {
        if (config instanceof HierarchicalNodeConfig nodeConfig) {
            return nodeConfig;
        } else {
            InvokerContainer<?> container = container();
            NodeConfig parentConfig = container == null ? null : container.config();
            HierarchicalNodeConfig nodeConfig = new HierarchicalNodeConfig(null, parentConfig);
            nodeConfig.set(config.items());
            return nodeConfig;
        }
    }

    public abstract String protocol();

    protected abstract T build(NodeConfig config);
}

