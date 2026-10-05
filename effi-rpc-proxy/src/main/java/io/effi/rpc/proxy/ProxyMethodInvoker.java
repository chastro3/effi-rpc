package io.effi.rpc.proxy;

import java.lang.reflect.Method;

/**
 * Dispatches raw proxy method calls to the framework invocation model.
 * <p>
 * Proxy dialect adapters forward every intercepted method to this contract. Implementations
 * resolve Object methods, interface default methods, and the configured {@link InvocationHandler}
 * before invoking user code.
 */
@FunctionalInterface
public interface ProxyMethodInvoker {

    /**
     * Dispatches one intercepted proxy method call.
     *
     * @param proxy  the proxy instance
     * @param method the invoked method
     * @param args   the method arguments
     * @return the method result
     * @throws Throwable if the invocation fails
     */
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
