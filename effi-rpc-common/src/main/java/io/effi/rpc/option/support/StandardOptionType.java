package io.effi.rpc.option.support;

import io.effi.rpc.option.OptionType;
import io.effi.rpc.util.AssertUtil;

import java.util.function.Function;

/**
 * Provides a standard scalar option type.
 */
public final class StandardOptionType<T> implements OptionType<T> {

    private final Function<Object, T> converter;

    public StandardOptionType(Function<Object, T> converter) {
        this.converter = AssertUtil.notNull(converter, "converter");
    }

    @Override
    public T convert(Object value) {
        return value == null ? null : converter.apply(value);
    }

}
