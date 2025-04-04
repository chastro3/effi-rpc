package io.effi.rpc.proxy.jdk;

import io.effi.rpc.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Adapts the JDK's {@link java.lang.reflect.InvocationHandler} to handle method invocations on proxy instances,
 * delegating the calls to a user-defined {@link InvocationHandler}.
 */
public class JDKInvocationHandler implements java.lang.reflect.InvocationHandler {

    private final Object target;

    private final InvocationHandler handler;

    public JDKInvocationHandler(Object target, InvocationHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return handler.invoke(proxy, method, args, superInvoker(method, args));
    }

    private Callable<?> superInvoker(Method method, Object[] args) {
        if (target instanceof Class<?>) {
            return () -> null;
        }
        return () -> method.invoke(target, args);
    }
}

