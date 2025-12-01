package io.effi.rpc.proxy.cglib;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Enhancer;

import static io.effi.rpc.proxy.cglib.CGLibProxyFactory.NAME;
import static io.effi.rpc.util.ClassUtil.findClassLoader;

/**
 * Implements {@link io.effi.rpc.proxy.ProxyFactory} using Cglib.
 */
@Extension(value = NAME, onClass = "org.springframework.cglib.proxy.Enhancer")
public class CGLibProxyFactory extends AbstractProxyFactory {

    public static final String NAME = "cglib";

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(findClassLoader(interfaceClass));
        enhancer.setSuperclass(interfaceClass);
        enhancer.setCallback(new CGLibMethodInterceptor(interfaceClass, handler));
        return (T) enhancer.create();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(findClassLoader(target.getClass()));
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new CGLibMethodInterceptor(target, handler));
        return (T) enhancer.create();
    }

}
