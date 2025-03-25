package io.effi.rpc.contract.parameter;

import io.effi.rpc.common.extension.Holder;
import io.effi.rpc.common.util.CollectionUtil;

import java.util.Map;

/**
 * Parameter variable.
 *
 * @param <T>
 */
public class ParamVar<T> extends Holder<T> implements Argument {

    ParamVar(T value) {
        super(value);
    }

    /**
     * Creates a ParamVar instance that wraps a Target object initialized with the provided map.
     *
     * @param map a map containing target parameters
     * @return a ParamVar wrapping the Target object
     */
    public static ParamVar<Target> target(Map<String, String> map) {
        Target target = new Target();
        if (CollectionUtil.isNotEmpty(map)) {
            target.set(map);
        }
        return new ParamVar<>(target);
    }

    /**
     * Creates a ParamVar instance that wraps a Source object with no name specified.
     * If it is a bean object, the value will be fetched from the parameters based on the field name
     *
     * @return a ParamVar wrapping a Source object with null name
     */
    public static ParamVar<Source> source() {
        return new ParamVar<>(new Source(null));
    }

    /**
     * Creates a ParamVar instance that wraps a Source object initialized with the specified name.
     *
     * @param name The name of the key in the parameter
     * @return a ParamVar wrapping a Source object with the specified name
     */
    public static ParamVar<Source> source(String name) {
        return new ParamVar<>(new Source(name));
    }

}

