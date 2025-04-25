package io.effi.rpc.contract.parameter;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.contract.RemoteService;

import java.lang.reflect.Method;

/**
 * Mapping methods to remote service calls.
 *
 * @param <T> the type of the remote service
 */
public class MethodMapper<T> {

    protected RemoteService<T> remoteService;

    protected Method method;

    protected ParameterMapper<ParameterParser<?>>[] parameterMappers;

    public MethodMapper(RemoteService<T> remoteService, Method method, ParameterMapper<ParameterParser<?>>[] parameterMappers) {
        this.remoteService = AssertUtil.notNull(remoteService, "remoteService");
        this.method = AssertUtil.notNull(method, "method");
        this.parameterMappers = parameterMappers == null ? ParameterMapper.emptyParsers(method) : parameterMappers;
    }

    /**
     * Returns the remoteService.
     *
     * @return the remoteService
     */
    public RemoteService<T> remoteService() {
        return remoteService;
    }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public Method method() {
        return method;
    }

    /**
     * Returns the parameterMappers.
     *
     * @return the parameterMappers
     */
    public ParameterMapper<ParameterParser<?>>[] parameterMappers() {
        return parameterMappers;
    }

    @Override
    public String toString() {
        return "<remoteService=" + remoteService + ", method=" + method + ">";
    }

}
