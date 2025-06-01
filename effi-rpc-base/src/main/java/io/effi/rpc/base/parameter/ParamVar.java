package io.effi.rpc.base.parameter;

import io.effi.rpc.util.Holder;
import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Wraps param variable for RPC argument resolution.
 */
public class ParamVar<T> extends Holder<T> implements Argument {

    ParamVar(T value) {
        super(value);
    }

    /**
     * Creates a ParamVar instance wrapping a Target object initialized with the provided map.
     */
    public static ParamVar<Target> target(Map<String, String> map) {
        Target target = new Target();
        if (CollectionUtil.isNotEmpty(map)) {
            target.set(map);
        }
        return new ParamVar<>(target);
    }

    /**
     * Creates a ParamVar instance wrapping a Source object with no name specified.
     * If it's a bean object, the value will be fetched from the parameters based on the field name.
     */
    public static ParamVar<Source> source() {
        return new ParamVar<>(new Source(null));
    }

    /**
     * Creates a ParamVar instance wrapping a Source object initialized with the specified name.
     */
    public static ParamVar<Source> source(String name) {
        return new ParamVar<>(new Source(name));
    }

}

