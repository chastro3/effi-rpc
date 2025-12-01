package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.concurrent.Future;

import java.net.InetAddress;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;
import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.config.OptionName.Strategy.MERGE_PARENT;

/**
 * Represents an RPC caller that initiate remote service invocations.
 * <p>
 * Provides the interface for making asynchronous and synchronous RPC calls,
 * managing client configurations, interceptors, and reply handling mechanisms.
 */
@ScopedComponent(scope = MODULE)
public interface Caller<R> extends Peer {

    OptionName<String> ENDPOINT = OptionName.of("endpoint", CURRENT_FIRST);

    OptionName<InetAddress> REMOTE_ADDRESS = OptionName.of("remoteAddress", CURRENT_FIRST);

    OptionName<String> REMOTE_PLATFORM = OptionName.of("remoteApplication", CURRENT_FIRST);

    OptionName<String> REMOTE_APPLICATION = OptionName.of("remoteApplication", CURRENT_FIRST);

    OptionName<String> REMOTE_MODULE = OptionName.of("remoteModule", CURRENT_FIRST);

    OptionName<String> LOCATOR = OptionName.of("locator", CURRENT_FIRST);

    OptionName<String> LOAD_BALANCER = OptionName.of("loadBalancer", CURRENT_FIRST);

    OptionName<String> SERVICE_DISCOVERY = OptionName.of("serviceDiscovery", CURRENT_FIRST);

    OptionName<String> ROUTER = OptionName.of("router", CURRENT_FIRST);

    OptionName<String> FAILURE_HANDLER = OptionName.of("failureHandler", CURRENT_FIRST);

    OptionName<String[]> REGISTRY = OptionName.of("registry", MERGE_PARENT);

    OptionName<String> CLIENT = OptionName.of("client", CURRENT_FIRST);

    OptionName<String> PROTOCOL = OptionName.of("protocol", CURRENT_FIRST);

    OptionName<Integer> TIMEOUT = OptionName.of("timeout", CURRENT_FIRST, 3000);

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






