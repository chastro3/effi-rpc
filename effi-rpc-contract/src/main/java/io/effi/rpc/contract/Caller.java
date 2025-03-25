package io.effi.rpc.contract;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.module.ModuleSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

/**
 * Client caller for making remote service calls.
 * <p>
 * Supports both asynchronous and synchronous RPC calls with flexible client-side configuration.
 *
 * @param <R> the return type of the remote service call
 */
public interface Caller<R> extends Invoker<CompletableFuture<R>>, ModuleSource {

    /**
     * Gets the client configuration used for the RPC call.
     *
     * @return the client configuration
     */
    ClientConfig clientConfig();

    /**
     * Gets the service discovery locator.
     *
     * @return the service locator
     */
    Locator locator();

    /**
     * Gets the thread pool used by the caller.
     *
     * @return the thread pool instance
     */
    ThreadPool threadPool();

    /**
     * Gets the module configuration for the invocation.
     *
     * @return the module config
     */
    CallerModularConfig modularConfig();

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
     * @param future a future object for the call result
     * @param <T>    a subtype of {@link ReplyFuture}
     * @return the provided future object containing the result
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
                String timeout = get(DefaultConfigKeys.TIMEOUT);
                throw EffiRpcException.wrap(PredefinedErrorCode.TIMEOUT, e, timeout);
            }
            throw EffiRpcException.wrap(PredefinedErrorCode.CALL_CALLER, e.getCause(), toString());
        }
    }
}






