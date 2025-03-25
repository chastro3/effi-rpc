package io.effi.rpc.proxy;

import io.effi.rpc.common.extension.spi.Extensible;

import static io.effi.rpc.common.constant.Component.ProxyFactory.JDK;

/**
 * Factory for creating proxy instances.
 */
@Extensible(JDK)
public interface ProxyFactory {

    /**
     * Creates a new proxy instance that implements the specified interface
     * using the provided invocation handler.
     *
     * @param interfaceClass the interface to be implemented by the proxy
     * @param handler        the invocation handler for method dispatch
     * @param <T>            the type of the interface
     * @return a new proxy object implementing the specified interface
     */
    <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler);

    /**
     * Creates a new proxy instance around the specified target object
     * using the provided invocation handler.
     *
     * @param target  the object to be proxied
     * @param handler the invocation handler for method dispatch
     * @param <T>     the type of the target object
     * @return a new proxy object wrapping the specified target
     */
    <T> T createProxy(T target, InvocationHandler handler);
}

