package io.effi.rpc.proxy.jdk;

import io.effi.rpc.proxy.ProxyMethodInvoker;

import java.lang.reflect.Method;

/**
 * Adapts JDK proxy invocations to the framework proxy dispatcher.
 */
public class JDKInvocationHandler implements java.lang.reflect.InvocationHandler {

    private final ProxyMethodInvoker invoker;

    public JDKInvocationHandler(ProxyMethodInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(proxy, method, args);
    }
}

