package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.async.Future;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.context.support.UnaryReplyFuture;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC caller that initiate remote service invocations.
 * <p>
 * Provides the interface for making asynchronous and synchronous RPC calls,
 * managing client configurations, interceptors, and reply handling mechanisms.
 */
@ScopedComponent(scope = MODULE)
public interface Caller<R> extends Peer {

    Interaction.Mode<UnaryReplyFuture> UNARY_MODE = UnaryReplyFuture::new;

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
     * Initiates an asynchronous RPC call with the specified arguments.
     *
     * @param args the arguments to pass to the remote service
     * @return a {@link Future} containing the result
     * @throws EffiRpcException if an error occurs during the call
     */
    Future<R> call(Object... args) throws EffiRpcException;


    @SuppressWarnings("unchecked")
    default <T> T blockingCall(Object... args) throws EffiRpcException {
        return (T) call(args).toCompletableFuture().join();
    }

}






