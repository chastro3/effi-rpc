package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines an RPC caller that initiates remote service invocations.
 * <p>
 * Provides the interface for making asynchronous and synchronous RPC calls,
 * managing client configurations, interceptors, and reply handling mechanisms.
 */
@ScopedComponent(scope = MODULE)
public interface Caller<R> extends Peer {

    /**
     * Returns the locator.
     */
    Locator locator();

    /**
     * Returns the client configuration.
     */
    ClientConfig clientConfig();

    /**
     * Returns the chosen interceptor chain.
     */
    Interceptor.Chain chosenInterceptorChain();

    /**
     * Initiates an asynchronous RPC call with positional arguments.
     *
     * @param args positional call arguments
     * @return future completing with the reply
     * @throws EffiRpcException when the call cannot be dispatched
     */
    default Future<R> call(Object... args) throws EffiRpcException {
        return call(new PositionalInvocation(args));
    }

    /**
     * Initiates an asynchronous RPC call with the specified arguments.
     *
     * @param invocation the method invocation to send
     * @return a {@link Future} containing the result
     * @throws EffiRpcException if an error occurs during the call
     */
    Future<R> call(Invocation invocation) throws EffiRpcException;

    /**
     * Performs the call with positional arguments and waits for its result.
     *
     * @param args positional call arguments
     * @param <T>  expected result type
     * @return reply value
     * @throws EffiRpcException when the call fails
     */
    default <T> T blockingCall(Object... args) throws EffiRpcException {
        return blockingCall(new PositionalInvocation(args));
    }

    /**
     * Performs the call and waits for its result.
     *
     * @param invocation method invocation to send
     * @param <T>        expected result type
     * @return reply value
     * @throws EffiRpcException when the call fails
     */
    @SuppressWarnings("unchecked")
    default <T> T blockingCall(Invocation invocation) throws EffiRpcException {
        return (T) call(invocation).toCompletableFuture().join();
    }

}




