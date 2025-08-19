package io.effi.rpc.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Handles method invocations on proxy instances.
 * <p>
 * Provides invocation handler functionality for intercepting and processing
 * method calls on proxy instances with support for invoking the original method.
 */
@FunctionalInterface
public interface InvocationHandler {

    /**
     * Intercepts a method call on the proxy and returns the result.
     *
     * @param proxy        the proxy instance
     * @param method       the invoked method
     * @param args         the method arguments
     * @param superInvoker invokes the original method
     * @return the method result
     * @throws Throwable if an error occurs
     */
    Object invoke(Object proxy, Method method, Object[] args, Callable<?> superInvoker) throws Throwable;
}



