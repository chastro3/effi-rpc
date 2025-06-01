package io.effi.rpc.proxy;

import io.effi.rpc.annotation.spi.Extensible;

import static io.effi.rpc.constant.Component.ProxyFactory.JDK;

/**
 * Creates proxy instance for specified interfaces or objects.
 */
@Extensible(JDK)
public interface ProxyFactory {

    /**
     * Creates a proxy for the specified interface.
     *
     * @param interfaceClass the interface to be implemented
     * @param handler        the invocation handler
     * @param <T>            the interface type
     * @return the proxy instance
     */
    <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler);

    /**
     * Creates a proxy for the specified object.
     *
     * @param target  the target object
     * @param handler the invocation handler
     * @param <T>     the object type
     * @return the proxy instance
     */
    <T> T createProxy(T target, InvocationHandler handler);
}


