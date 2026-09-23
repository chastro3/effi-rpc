package io.effi.rpc.option.support;

import io.effi.rpc.option.ArrayOptionType;
import io.effi.rpc.util.AssertUtil;

import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Provides a standard array option type.
 *
 * @param <A> the array type
 */
public final class StandardArrayOptionType<A> implements ArrayOptionType<A> {

    private final Function<Object, A> converter;

    private final BinaryOperator<A> merger;

    public StandardArrayOptionType(
            Function<Object, A> converter,
            BinaryOperator<A> merger
    ) {
        this.converter = AssertUtil.notNull(converter, "converter");
        this.merger = AssertUtil.notNull(merger, "merger");
    }

    @Override
    public A convert(Object value) {
        return value == null ? null : converter.apply(value);
    }

    @Override
    public A merge(A parent, A current) {
        if (parent == null) {
            return current;
        }
        if (current == null) {
            return parent;
        }
        return merger.apply(parent, current);
    }
}
