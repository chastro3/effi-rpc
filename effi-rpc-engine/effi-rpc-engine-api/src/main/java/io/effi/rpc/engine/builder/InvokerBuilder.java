package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.extension.ChainBuilder;
import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigSource;
import io.effi.rpc.common.util.CollectionUtil;
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
     * Sets the compression method.
     *
     * @param compression Compression type
     * @return This builder
     */
    public C compression(String compression) {
        config.set(DefaultConfigKeys.COMPRESSION.key(), compression);
        return returnThis();
    }

    /**
     * Sets the serialization format.
     *
     * @param serialization Serialization format
     * @return This builder
     */
    public C serialization(String serialization) {
        config.set(DefaultConfigKeys.SERIALIZATION.key(), serialization);
        return returnThis();
    }

    /**
     * Sets the query path for the invoker.
     *
     * @param path Query path string
     * @return This builder
     */
    public C path(String path) {
        config.set(DefaultConfigKeys.PATH.key(), path);
        return returnThis();
    }

    /**
     * Adds filters to the invoker, avoiding duplicates.
     *
     * @param filters Filters to add
     * @return This builder
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

    /**
     * Builds and returns the configured {@link Invoker} instance.
     *
     * @return Configured {@link Invoker} instance
     */
    @Override
    public T build() {
        return build(config());
    }

    /**
     * Defines the protocol used by the invoker.
     *
     * @return Protocol string
     */
    public abstract String protocol();

    /**
     * Constructs the invoker instance with the given URL.
     *
     * @param config config for the invoker
     * @return New {@link Invoker} instance
     */
    protected abstract T build(Config config);
}

