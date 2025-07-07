package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.exception.EffiRpcException;

import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC caller that can call a remote callee.
 */
@ScopedComponent(scope = MODULE)
public interface Caller<R> extends CallSide {

    /**
     * Returns the client configuration.
     */
    ClientConfig clientConfig();

    /**
     * Returns the chosen interceptor chain.
     */
    InterceptorChain chosenInterceptorChain();

    /**
     * Returns the reply stage chain.
     */
    StageChain replyStageChain();

    /**
     * Returns the reply interceptor chain.
     */
    InterceptorChain replyInterceptorChain();

    /**
     * Returns the locator.
     */
    Locator locator();

    /**
     * Initiates an asynchronous RPC call with the specified arguments.
     *
     * @param args the arguments to pass to the remote service
     * @return a {@link CompletableFuture} containing the result
     * @throws EffiRpcException if an error occurs during the call
     */
    CompletableFuture<R> call(Object... args) throws EffiRpcException;

    /**
     * Initiates a synchronous RPC call with the specified arguments.
     *
     * @param args the arguments to pass to the remote service
     * @return the result of the synchronous call
     * @throws EffiRpcException if an error occurs during the call
     */
    R blockingCall(Object... args) throws EffiRpcException;

    /**
     * Initiates a call using the provided future (without recreating the context).
     *
     * @param future the future
     * @param <T>    a subtype of {@link ReplyFuture}
     * @return the provided future
     * @throws EffiRpcException if an error occurs during the call
     */
    <T extends ReplyFuture> T callWithFuture(T future) throws EffiRpcException;
}






