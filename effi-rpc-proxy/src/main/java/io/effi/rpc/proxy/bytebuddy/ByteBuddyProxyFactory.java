package io.effi.rpc.proxy.bytebuddy;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.ProxyMethodInvoker;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandles;

import static io.effi.rpc.proxy.bytebuddy.ByteBuddyProxyFactory.NAME;

/**
 * Implements {@link io.effi.rpc.proxy.ProxyFactory} using ByteBuddy.
 * <p>
 * Instance proxies require the target class to declare a visible no-arg constructor.
 */
@Extension(value = NAME, onClass = "net.bytebuddy.ByteBuddy")
public class ByteBuddyProxyFactory extends AbstractProxyFactory {

    public static final String NAME = "bytebuddy";

    @Override
    protected <T> T doCreateProxy(Class<T> interfaceClass, ProxyMethodInvoker invoker) throws Exception {
        try (DynamicType.Unloaded<T> dynamicType = new ByteBuddy()
                .subclass(interfaceClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodInterceptor(invoker)))
                .make()) {
            return dynamicType.load(interfaceClass.getClassLoader(), loadingStrategy(interfaceClass))
                    .getLoaded().getDeclaredConstructor().newInstance();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, ProxyMethodInvoker invoker) throws Exception {
        try (DynamicType.Unloaded<T> dynamicType = (DynamicType.Unloaded<T>) new ByteBuddy()
                .subclass(target.getClass())
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodInterceptor(invoker)))
                .make()) {
            return dynamicType.load(target.getClass().getClassLoader(), loadingStrategy(target.getClass()))
                    .getLoaded().getConstructor().newInstance();
        }
    }

    private ClassLoadingStrategy<ClassLoader> loadingStrategy(Class<?> type) throws IllegalAccessException {
        return ClassLoadingStrategy.UsingLookup.of(MethodHandles.privateLookupIn(type, MethodHandles.lookup()));
    }
}
