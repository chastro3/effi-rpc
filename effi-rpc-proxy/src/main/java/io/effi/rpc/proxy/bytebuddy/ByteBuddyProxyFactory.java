package io.effi.rpc.proxy.bytebuddy;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.util.ClassUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import static io.effi.rpc.proxy.bytebuddy.ByteBuddyProxyFactory.NAME;

/**
 * Implements {@link io.effi.rpc.proxy.ProxyFactory} using ByteBuddy.
 */
@Extension(value = NAME, onClass = "net.bytebuddy.ByteBuddy")
public class ByteBuddyProxyFactory extends AbstractProxyFactory {

    public static final String NAME = "bytebuddy";

    @Override
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        try (DynamicType.Unloaded<T> dynamicType = new ByteBuddy()
                .subclass(interfaceClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodInterceptor(handler).new InterfaceInterceptor()))
                .make()) {
            return dynamicType.load(ClassUtil.findClassLoader(interfaceClass))
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
            return dynamicType.load(ClassUtil.findClassLoader(target.getClass()))
                    .getLoaded().getConstructor().newInstance();
        }
    }
}
