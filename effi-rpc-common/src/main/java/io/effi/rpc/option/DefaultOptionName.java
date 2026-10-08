package io.effi.rpc.option;

import io.effi.rpc.util.AssertUtil;

/**
 * Provides the default implementation of {@link OptionName}.
 */
record DefaultOptionName<T>(String name, OptionStrategy strategy, T defaultValue,
                            OptionType<T> type) implements OptionName<T> {

    DefaultOptionName(String name, OptionStrategy strategy, T defaultValue, OptionType<T> type) {
        this.name = AssertUtil.notBlank(name, "name");
        this.strategy = AssertUtil.notNull(strategy, "strategy");
        this.defaultValue = defaultValue;
        this.type = AssertUtil.notNull(type, "type");
    }
}
