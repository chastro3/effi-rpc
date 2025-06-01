package io.effi.rpc.base;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Represents an RPC caller that can call a remote callee.
 */
@ScopedComponent(scope = MODULE)
public interface Caller<R> extends Invoker<CompletableFuture<R>> {

    /**
     * Returns the client configuration.
     */
    ClientConfig clientConfig();

    /**
     * Returns the registry configurations.
     */
    List<RegistryConfig> registryConfigs();

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
     * Initiates a call using the provided future (without recreating the context).
     *
     * @param future the future
     * @param <T>    a subtype of {@link ReplyFuture}
     * @return the provided future
     * @throws EffiRpcException if an error occurs during the call
     */
    <T extends ReplyFuture> T callWithFuture(T future) throws EffiRpcException;

    /**
     * Initiates a synchronous RPC call with the specified arguments.
     *
     * @param args the arguments to pass to the remote service
     * @return the result of the synchronous call
     * @throws EffiRpcException if an error occurs during the call
     */
    default R blockingCall(Object... args) throws EffiRpcException {
        try {
            return call(args).join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof TimeoutException) {
                String timeout = config().get(DefaultConfigKeys.TIMEOUT);
                throw PredefinedErrorCode.TIMEOUT.fail(e, timeout);
            }
            throw PredefinedErrorCode.CALL_CALLER.fail(e.getCause(), toString());
        }
    }
}






