package io.effi.rpc.context.parameter;

import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;

/**
 * Maps methods to remote service calls.
 */
public record ServantMethod<T>(ServantGroup<T> group, Method method, ParameterBinding[] bindings) {

    public ServantMethod(ServantGroup<T> group, Method method, ParameterBinding[] bindings) {
        this.group = AssertUtil.notNull(group, "group");
        this.method = AssertUtil.notNull(method, "method");
        this.bindings = bindings == null ? ParameterBinding.emptyResolvers(method) : bindings;
    }

    @Override
    public String toString() {
        return "group=" + group + ", method=" + method;
    }

}
