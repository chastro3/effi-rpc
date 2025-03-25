package io.effi.rpc.proxy.jdk;

import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;

import java.lang.reflect.Proxy;

import static io.effi.rpc.common.constant.Component.ProxyFactory.JDK;

/**
 * {@link io.effi.rpc.proxy.ProxyFactory} implementation based on jdk.
 */
@Extension(JDK)
public class JDKProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new JDKInvocationHandler(interfaceClass, handler)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception {
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new JDKInvocationHandler(target, handler)
        );
    }
}

