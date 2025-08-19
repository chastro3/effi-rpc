package io.effi.rpc.context.parameter;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.Holder;

import java.util.Map;

/**
 * Wraps path variable for RPC argument resolution.
 */
public class PathVar<T> extends Holder<T> implements Argument {

    PathVar(T value) {
        super(value);
    }

    /**
     * Creates a PathVar instance that wraps a Target object initialized with the provided map.
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
     */
    public static PathVar<Source> source(String name) {
        return new PathVar<>(new Source(name));
    }
}
