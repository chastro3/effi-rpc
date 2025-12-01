package io.effi.rpc.proxy.jdk;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.util.ClassUtil;

import java.lang.reflect.Proxy;

import static io.effi.rpc.proxy.jdk.JDKProxyFactory.NAME;

/**
 * Implements {@link io.effi.rpc.proxy.ProxyFactory} using Jdk.
 */
@Extension(value = NAME, primary = true)
public class JDKProxyFactory extends AbstractProxyFactory {

    public static final String NAME = "jdk";

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        return (T) Proxy.newProxyInstance(
                ClassUtil.findClassLoader(interfaceClass),
                new Class[]{interfaceClass},
                new JDKInvocationHandler(interfaceClass, handler)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception {
        return (T) Proxy.newProxyInstance(
                ClassUtil.findClassLoader(target.getClass()),
                target.getClass().getInterfaces(),
                new JDKInvocationHandler(target, handler)
        );
    }
}

