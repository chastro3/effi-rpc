package io.effi.rpc.proxy;

import java.lang.reflect.Method;

/**
 * Handles method invocations on a proxy instance.
 */
@FunctionalInterface
public interface InvocationHandler {

    /**
     * Processes the method invocation on the given proxy instance and returns the result.
     *
     * @param proxy        the proxy instance where the method was invoked
     * @param method       the method being invoked
     * @param args         the arguments passed to the method
     * @param superInvoker used to invoke the original method
     * @return the result of the method invocation
     * @throws Throwable if an error occurs during processing
     */
    Object invoke(Object proxy, Method method, Object[] args, SuperInvoker<?> superInvoker) throws Throwable;
}


