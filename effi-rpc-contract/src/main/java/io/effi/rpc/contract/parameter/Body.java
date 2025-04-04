package io.effi.rpc.contract.parameter;

import io.effi.rpc.common.util.Holder;

/**
 * Message body identify.
 *
 * @param <T>
 */
public class Body<T> extends Holder<T> implements Argument {

    public static final Body<?> IDENTIFY = wrap(null);

    Body(T value) {
        super(value);
    }

    /**
     * Wrap body.
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> Body<T> wrap(T value) {
        return new Body<>(value);
    }

}
