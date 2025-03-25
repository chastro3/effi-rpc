package io.effi.rpc.contract.parameter;

import io.effi.rpc.common.extension.Holder;
import io.effi.rpc.common.util.CollectionUtil;

import java.util.Map;

/**
 * Path variable.
 *
 * @param <T>
 */
public class PathVar<T> extends Holder<T> implements Argument {

    PathVar(T value) {
        super(value);
    }

    /**
     * Creates a PathVar instance that wraps a Target object initialized with the provided map.
     *
     * @param map a map containing target parameters
     * @return a ParamVar wrapping the Target object
     */
    public static PathVar<Target> target(Map<String, String> map) {
        Target target = new Target();
        if (CollectionUtil.isNotEmpty(map)) {
            target.set(map);
        }
        return new PathVar<>(target);
    }

    /**
     * Creates a PathVar instance that wraps a Source object initialized with the specified name.
     *
     * @param name The name of the path
     * @return a ParamVar wrapping a Source object with the specified name
     */
    public static PathVar<Source> source(String name) {
        return new PathVar<>(new Source(name));
    }
}
