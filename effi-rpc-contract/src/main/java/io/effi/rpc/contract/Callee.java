package io.effi.rpc.contract;

import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Server callee for handling remote service calls.
 * <p>
 * Responsible for invoking remote service methods and mapping parameters for the invocation.
 *
 * @param <T> the type of the remote service interface
 */
public interface Callee<T> extends Invoker<Object> {

    /**
     * Returns the remote service associated with this callee.
     */
    RemoteService<T> remoteService();

    /**
     * Returns the method represented by this callee.
     */
    Method method();

    /**
     * Returns the index of the method within the remote service.
     */
    int methodIndex();

    /**
     * Invokes the current call using the specified invocation context.
     *
     * @param context the invocation context containing the request and callee information
     * @return the reply context resulting from the invocation
     */
    ReplyContext<Envelope.Response, Callee<?>> invokeWithContext(InvocationContext<Envelope.Request, Callee<?>> context);

    /**
     * Returns the parameter mappers for the method.
     */
    ParameterMapper<ParameterParser<?>>[] parameterMappers();

    /**
     * Retrieves the thread pool associated with the given module.
     */
    ThreadPool threadPoolOf(EffiRpcModule module);

    /**
     * Returns a description of this server callee.
     */
    String desc();

    /**
     * Exposes the current callee to the specified module(s).
     *
     * @param modules the modules to which the callee will be exposed
     */
    void export(EffiRpcModule... modules);

    /**
     * Returns the exported modules.
     */
    Collection<EffiRpcModule> exportedModules();

    /**
     * Returns the manager key for this callee.
     * If the query path is not available, returns an empty string.
     *
     * @return the manager key derived from the query path
     */
    @Override
    default String managerKey() {
        return queryPath() == null ? "" : queryPath().path();
    }
}



