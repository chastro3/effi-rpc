package io.effi.rpc.contract.parameter;

import io.effi.rpc.common.util.Holder;
import io.effi.rpc.common.util.CollectionUtil;

import java.util.Map;

/**
 * Header variable.
 *
 * @param <T>
 */
public class Header<T> extends Holder<T> implements Argument {

    public Header(T value) {
        super(value);
    }

    /**
     * Creates a Header instance that wraps a Target object initialized with the provided map.
     *
     * @param map a map containing target headers
     * @return a Header wrapping the Target object
     */
    public static Header<Target> target(Map<String, String> map) {
        Target target = new Target();
        if (CollectionUtil.isNotEmpty(map)) {
            target.set(map);
        }
        return new Header<>(target);
    }

    /**
     * Creates a Header instance that wraps a Source object with no name specified.
     * If it is a bean object, the value will be fetched from the headers based on the field name
     *
     * @return a Header wrapping a Source object with null name
     */
    public static Header<Source> source() {
        return new Header<>(new Source(null));
    }

    /**
     * Creates a Header instance that wraps a Source object initialized with the specified name.
     *
     * @param name The name of the key in the header
     * @return a Header wrapping a Source object with the specified name
     */
    public static Header<Source> source(String name) {
        return new Header<>(new Source(name));
    }

}
