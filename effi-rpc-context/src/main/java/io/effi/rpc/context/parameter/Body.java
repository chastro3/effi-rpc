package io.effi.rpc.context.parameter;

import io.effi.rpc.util.Holder;

/**
 * Wraps message body for RPC argument resolution.
 */
public class Body<T> extends Holder<T> implements Argument {

    public static final Body<?> IDENTIFY = wrap(null);

    Body(T value) {
        super(value);
    }

    /**
     * Wraps the given value into a Body instance.
     *
     * @param value body value
     * @return body wrapper
     */
    public static <T> Body<T> wrap(T value) {
        return new Body<>(value);
    }

}

