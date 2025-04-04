package io.effi.rpc.proxy;

import io.effi.rpc.common.exception.PredefinedErrorCode;

import static io.effi.rpc.common.util.ReflectionUtil.invokeObjectMethod;

/**
 * Abstract implementation of {@link ProxyFactory}.
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        try {
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException("The method only support interface");
            }
            return doCreateProxy(interfaceClass, wrap(interfaceClass, handler));
        } catch (Exception e) {
            throw PredefinedErrorCode.PROXY_CREATE.fail(e, interfaceClass.getName());
        }
    }

    @Override
    public <T> T createProxy(T target, InvocationHandler handler) {
        try {
            return doCreateProxy(target, wrap(target, handler));
        } catch (Exception e) {
            throw PredefinedErrorCode.PROXY_CREATE.fail(e, target.getClass().getName());
        }
    }

    protected abstract <T> T doCreateProxy(Class<T> interfaceClass, InvocationHandler handler) throws Exception;

    protected abstract <T> T doCreateProxy(T target, InvocationHandler handler) throws Exception;

    private InvocationHandler wrap(Object target, InvocationHandler invocationHandler) {
        // todo 提供一个Wrapper接口
        return (proxy, method, args, superInvoker) -> {
            if (Object.class.equals(method.getDeclaringClass())) {
                return invokeObjectMethod(target, method, args);
            }
            return invocationHandler.invoke(proxy, method, args, superInvoker);
        };
    }

}
