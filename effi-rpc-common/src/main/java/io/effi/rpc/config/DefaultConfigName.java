package io.effi.rpc.config;

import io.effi.rpc.util.LazySingleton;

import java.util.function.Supplier;

/**
 * Provides the default implementation of {@link ConfigName}.
 */
public class DefaultConfigName<V> implements ConfigName<V> {

    private final String name;
    private final Strategy strategy;
    private final boolean nullable;
    private final V eagerDefaultValue;
    private LazySingleton<V> lazyDefaultValue;

    public static <V> ConfigName<V> nameOf(String name) {
        return nameOf(name, true);
    }

    public static <V> ConfigName<V> nameOf(String name, boolean nullable) {
        return nameOf(name, Strategy.ONLY_CURRENT, nullable);
    }


    public static <V> ConfigName<V> nameOf(String name, Strategy strategy) {
        return nameOf(name, strategy, true);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, boolean nullable) {
        return nameOf(name, strategy, nullable, (V) null);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, V defaultValue) {
        return nameOf(name, strategy, true, defaultValue);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, boolean nullable, V defaultValue) {
        return new DefaultConfigName<>(name, strategy, nullable, defaultValue);
    }

    public static <V> ConfigName<V> nameOf(String name, Strategy strategy, boolean nullable, Supplier<V> defaultValueProvider) {
        return new DefaultConfigName<>(name, strategy, nullable, defaultValueProvider);
    }


    private DefaultConfigName(String name, Strategy strategy, boolean nullable, V defaultValue) {
        this.name = name;
        this.strategy = strategy;
        this.nullable = nullable;
        this.eagerDefaultValue = defaultValue;
        this.lazyDefaultValue = null;
    }

    private DefaultConfigName(String name, Strategy strategy, boolean nullable, Supplier<V> defaultValueProvider) {
        this.name = name;
        this.strategy = strategy;
        this.nullable = nullable;
        this.eagerDefaultValue = null;
        if (defaultValueProvider != null) {
            this.lazyDefaultValue = LazySingleton.from(defaultValueProvider);
        }
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
    public boolean nullable() {
        return nullable;
    }

    @Override
    public V defaultValue() {
        if (eagerDefaultValue != null) {
            return eagerDefaultValue;
        }
        return lazyDefaultValue != null ? lazyDefaultValue.ensure() : null;
    }
}
