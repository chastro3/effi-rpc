package io.effi.rpc.proxy;

import io.effi.rpc.annotation.component.Extensible;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Creates dynamic proxies backed by an {@link InvocationHandler}.
 * <p>
 * Implementations are platform-scoped extensions selected by proxy name.
 */
@Extensible(scope = PLATFORM)
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
     * <p>
     * Subclass-based dialects require the target class to declare a visible no-arg constructor.
     *
     * @param target  the target object
     * @param handler the invocation handler
     * @param <T>     the object type
     * @return the proxy instance
     */
    <T> T createProxy(T target, InvocationHandler handler);
}


