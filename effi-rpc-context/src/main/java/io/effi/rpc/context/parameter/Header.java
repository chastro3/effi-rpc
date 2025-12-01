package io.effi.rpc.context.parameter;

import io.effi.rpc.util.Holder;
import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Wraps header for RPC argument resolution.
 */
public class Header<T> extends Holder<T> implements Argument {

    public Header(T value) {
        super(value);
    }

    /**
     * Creates a Header instance that wraps a Target object initialized with the provided map.
     */
    public static Header<Target> target(Map<String, String> map) {
        Target target = new Target();
        if (CollectionUtil.isNotEmpty(map)) {
            target.set(map);
        }
        return new Header<>(target);
    }

    /**
     * Creates a Header instance that wraps a Source object with no id specified.
     * If it is a bean object, the value will be fetched from the headers based on the field id.
     */
    public static Header<Source> source() {
        return new Header<>(new Source(null));
    }

    /**
     * Creates a Header instance that wraps a Source object initialized with the specified id.
     */
    public static Header<Source> source(String name) {
        return new Header<>(new Source(name));
    }

}

