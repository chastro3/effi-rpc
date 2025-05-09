package io.effi.rpc.proxy.cglib;

import io.effi.rpc.proxy.InvocationHandler;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Adapts CGLIB's {@link MethodInterceptor} to handle method invocations on proxies.
 * <p>
 * Delegates method calls to a user-defined {@link InvocationHandler}.
 * </p>
 */
public class CGLibMethodInterceptor implements MethodInterceptor {

    private final Object target;

    private final InvocationHandler handler;

    public CGLibMethodInterceptor(Object target, InvocationHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return handler.invoke(proxy, method, args, superInvoker(obj, proxy, args));

    }

    private Callable<?> superInvoker(Object obj, MethodProxy proxy, Object[] args) {
        if (target instanceof Class<?>) {
            return () -> null;
        }
        return () -> {
            try {
                return proxy.invokeSuper(obj, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

}
