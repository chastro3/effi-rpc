package io.effi.rpc.proxy.bytebuddy;

import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import static io.effi.rpc.common.constant.Component.ProxyFactory.BYTEBUDDY;

/**
 * ByteBuddy implementation of {@link ProxyFactory}.
 */
@Extension(BYTEBUDDY)
public class ByteBuddyProxyFactory extends AbstractProxyFactory {

    @Override
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        try (DynamicType.Unloaded<T> dynamicType = new ByteBuddy()
                .subclass(interfaceClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodInterceptor(handler).new InterfaceInterceptor()))
                .make()) {
            return dynamicType.load(interfaceClass.getClassLoader())
                    .getLoaded().getDeclaredConstructor().newInstance();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception {
        try (DynamicType.Unloaded<T> dynamicType = (DynamicType.Unloaded<T>) new ByteBuddy()
                .subclass(target.getClass())
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodInterceptor(handler).new InstanceInterceptor()))
                .make()) {
            return dynamicType.load(target.getClass().getClassLoader())
                    .getLoaded().getConstructor().newInstance();
        }
    }
}
