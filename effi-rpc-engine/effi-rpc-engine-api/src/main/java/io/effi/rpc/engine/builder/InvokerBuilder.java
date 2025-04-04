package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigSource;
import io.effi.rpc.common.util.ChainBuilder;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Invoker;
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

    protected Config config;

    protected List<Filter<?, ?, ?>> filters = new ArrayList<>();

    protected TypeToken<?> returnType;

    protected InvokerBuilder(Config config) {
        this.config = config == null ? new Config() : config;
    }

    /**
     * Sets the compression type.
     */
    public C compression(String compression) {
        config.set(DefaultConfigKeys.COMPRESSION.key(), compression);
        return returnThis();
    }

    /**
     * Sets the serialization type.
     */
    public C serialization(String serialization) {
        config.set(DefaultConfigKeys.SERIALIZATION.key(), serialization);
        return returnThis();
    }

    /**
     * Sets the query path for the invoker.
     */
    public C path(String path) {
        config.set(DefaultConfigKeys.PATH.key(), path);
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

    /**
     * Returns the returnType.
     */
    public TypeToken<?> returnType() {
        return returnType;
    }

    /**
     * Returns the filters.
     */
    public List<Filter<?, ?, ?>> filters() {
        return filters;
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public T build() {
        return build(config());
    }

    public abstract String protocol();

    protected abstract T build(Config config);
}

