package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC callee that handle remote service invocations.
 * <p>
 * Provides the interface for receiving and processing RPC calls,
 * managing remote services, methods, and parameter mapping.
 */
@ScopedComponent(scope = MODULE)
public interface Callee extends Peer {

    /**
     * Returns the remote service associated with this callee.
     */
    RemoteService<?> remoteService();

    /**
     * Returns the method associated with this callee.
     */
    Method method();

    /**
     * Returns the index of the method in the remote service.
     */
    int methodIndex();

    /**
     * Invokes the method with given arguments.
     *
     * @param args arguments to pass to the method
     * @return the result of the invocation
     */
    Object invoke(Object... args);

    /**
     * Returns parameter mappers for the method.
     */
    ParameterMapper<ParameterParser<?>>[] parameterMappers();

    /**
     * Returns a description of this callee.
     */
    String desc();
}



