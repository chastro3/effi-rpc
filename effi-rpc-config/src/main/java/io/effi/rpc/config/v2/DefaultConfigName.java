package io.effi.rpc.config.v2;

import io.effi.rpc.util.LazyInitializer;

import java.util.function.Supplier;

public class DefaultConfigName<V> implements ConfigName<V> {

    private final String name;
    private final Strategy strategy;
    private final V eagerDefaultValue;
    private final LazyInitializer<V> lazyDefaultValue;

    public static <V> ConfigName<V> nameOf(String name) {
        return new DefaultConfigName<>(name, Strategy.SELF_ONLY, null);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy) {
        return new DefaultConfigName<>(name, strategy, null);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, V defaultValue) {
        return new DefaultConfigName<>(name, strategy, defaultValue);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, Supplier<V> defaultValueProvider) {
        return new DefaultConfigName<>(name, strategy, defaultValueProvider);
    }


    public DefaultConfigName(String name, Strategy strategy, V defaultValue) {
        this.name = name;
        this.strategy = strategy;
        this.eagerDefaultValue = defaultValue;
        this.lazyDefaultValue = null;
    }

    public DefaultConfigName(String name, Strategy strategy, Supplier<V> defaultValueProvider) {
        this.name = name;
        this.strategy = strategy;
        this.eagerDefaultValue = null;
        this.lazyDefaultValue = new LazyInitializer<>(defaultValueProvider);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ConfigName.Strategy strategy() {
        return strategy;
    }

    @Override
    public V defaultValue() {
        if (eagerDefaultValue != null) {
            return eagerDefaultValue;
        }
        return lazyDefaultValue != null ? lazyDefaultValue.get() : null;
    }
}
