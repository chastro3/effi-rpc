package io.effi.rpc.base.parameter;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.base.RemoteService;

import java.lang.reflect.Method;

/**
 * Maps methods to remote service calls.
 */
public record MethodMapper<T>(RemoteService<T> remoteService, Method method,
                              ParameterMapper<ParameterParser<?>>[] parameterMappers) {

    public MethodMapper(RemoteService<T> remoteService, Method method, ParameterMapper<ParameterParser<?>>[] parameterMappers) {
        this.remoteService = AssertUtil.notNull(remoteService, "remoteService");
        this.method = AssertUtil.notNull(method, "method");
        this.parameterMappers = parameterMappers == null ? ParameterMapper.emptyParsers(method) : parameterMappers;
    }

    @Override
    public String toString() {
        return "remoteService=" + remoteService + ", method=" + method;
    }

}
