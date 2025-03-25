package io.effi.rpc.proxy.cglib;

import io.effi.rpc.common.extension.spi.Extension;
import io.effi.rpc.proxy.AbstractProxyFactory;
import io.effi.rpc.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Enhancer;

import static io.effi.rpc.common.constant.Component.ProxyFactory.CGLIB;
import static io.effi.rpc.common.util.ClassUtil.getClassLoader;

/**
 * {@link io.effi.rpc.proxy.ProxyFactory} implementation based on cglib.
 */
@Extension(CGLIB)
public class CGLibProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(getClassLoader(interfaceClass));
        enhancer.setSuperclass(interfaceClass);
        enhancer.setCallback(new CGLibMethodInterceptor(interfaceClass, handler));
        return (T) enhancer.create();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(getClassLoader(target.getClass()));
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new CGLibMethodInterceptor(target, handler));
        return (T) enhancer.create();
    }

}
