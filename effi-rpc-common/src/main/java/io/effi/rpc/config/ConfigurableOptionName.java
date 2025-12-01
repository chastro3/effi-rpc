package io.effi.rpc.config;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.LazySingleton;

import java.util.function.Supplier;

/**
 * Provides the default implementation of {@link OptionName}.
 */
public class ConfigurableOptionName<V> implements OptionName<V> {

    private final String name;
    private final Strategy strategy;
    private V eagerDefaultValue;
    private LazySingleton<V> lazyDefaultValue;

    public static <V> ConfigurableOptionName<V> nameOf(String name) {
        return nameOf(name, Strategy.ONLY_CURRENT);
    }

    public static <V> ConfigurableOptionName<V> nameOf(String name, Strategy strategy) {
        return new ConfigurableOptionName<>(name, strategy);
    }

    public ConfigurableOptionName(String name, Strategy strategy) {
        this.name = AssertUtil.notBlank(name, "name");
        this.strategy = AssertUtil.notNull(strategy, "strategy");
    }

    public ConfigurableOptionName<V> defaultValue(V defaultValue) {
        this.eagerDefaultValue = defaultValue;
        return this;
    }

    public ConfigurableOptionName<V> defaultValue(Supplier<V> defaultValueProvider) {
        this.lazyDefaultValue = LazySingleton.from(defaultValueProvider);
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Strategy strategy() {
        return strategy;
    }

    @Override
    public V defaultValue() {
        if (eagerDefaultValue != null) {
            return eagerDefaultValue;
        }
        return lazyDefaultValue != null ? lazyDefaultValue.ensure() : null;
    }
}
