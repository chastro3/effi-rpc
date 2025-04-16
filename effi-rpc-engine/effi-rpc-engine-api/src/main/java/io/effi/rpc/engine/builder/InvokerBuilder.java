package io.effi.rpc.engine.builder;

import io.effi.rpc.common.config.ConfigSource;
import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.config.HierarchicalNodeConfig;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.ChainBuilder;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.InvokerContainer;
import io.effi.rpc.contract.filter.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating {@link Invoker} instances,defining settings for invoker.
 *
 * @param <T> The type of {@link Invoker}.
 * @param <C> The type of the builder.
 */
public abstract class InvokerBuilder<T extends Invoker<?>, C extends InvokerBuilder<T, C>>
        implements ChainBuilder<T, C>, ConfigSource {

    protected NodeConfig config;

    protected InvokerContainer<?> container;

    protected List<Filter<?, ?, ?>> filters = new ArrayList<>();

    protected TypeToken<?> returnType;

    protected InvokerBuilder(NodeConfig config) {
        this.config = AssertUtil.notNull(config, "config");
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
     * Adds filters to the invoker, avoiding duplicates.
     */
    public C addFilter(Filter<?, ?, ?>... filters) {
        if (CollectionUtil.isNotEmpty(filters)) {
            CollectionUtil.addUnique(this.filters, filters);
        }
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

