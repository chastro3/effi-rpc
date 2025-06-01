package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC callee that can be called by a caller.
 */
@ScopedComponent(scope = MODULE)
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
     * Invokes the current callee using the specified invocation context.
     *
     * @param context the invocation context
     * @return the reply context
     */
    ReplyContext<Envelope.Response, Callee<?>> invokeWithContext(InvocationContext<Envelope.Request, Callee<?>> context);

    /**
     * Returns the parameter mappers for the method.
     */
    ParameterMapper<ParameterParser<?>>[] parameterMappers();

    /**
     * Returns a description of this callee.
     */
    String desc();
}



