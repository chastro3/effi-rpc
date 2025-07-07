package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.base.context.ReplyContext;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC callee that can be called by remote caller(s).
 */
@ScopedComponent(scope = MODULE)
public interface Callee extends CallSide {

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
     * Invokes this callee using the given invocation context.
     *
     * @param context the invocation context
     * @return the reply context
     */
    ReplyContext<Message.Response, Callee> invokeWithContext(CallContext<Message.Request, Callee> context);

    /**
     * Returns parameter mappers for the method.
     */
    ParameterMapper<ParameterParser<?>>[] parameterMappers();

    /**
     * Returns a description of this callee.
     */
    String desc();
}



