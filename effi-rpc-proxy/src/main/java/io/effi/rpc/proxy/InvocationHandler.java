package io.effi.rpc.proxy;

import java.lang.reflect.Method;

/**
 * Handles method invocations on proxy instances.
 * <p>
 * Implementations receive the generated proxy, the invoked method, its arguments, and a
 * {@link SuperInvoker} that invokes the original implementation when the proxy wraps a target.
 */
@FunctionalInterface
public interface InvocationHandler {

    /**
     * Intercepts a method call on the proxy and returns the invocation result.
     *
     * @param proxy        the proxy instance
     * @param method       the invoked method
     * @param args         the method arguments
     * @param superInvoker invokes the original implementation when available
     * @return the method result
     * @throws Throwable if the invocation fails
     */
    Object invoke(Object proxy, Method method, Object[] args, SuperInvoker superInvoker) throws Throwable;

    /**
     * Invokes the original implementation behind a proxy.
     */
    @FunctionalInterface
    interface SuperInvoker {

        /**
         * Invokes the original implementation and returns its result.
         *
         * @return the original method result
         * @throws Throwable if the original invocation fails
         */
        Object invoke() throws Throwable;
    }
}



