package io.effi.rpc.proxy.bytebuddy;

import io.effi.rpc.proxy.ProxyMethodInvoker;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

/**
 * Intercepts ByteBuddy proxy invocations and forwards them to the framework proxy dispatcher.
 */
public class MethodInterceptor {

    private final ProxyMethodInvoker invoker;

    public MethodInterceptor(ProxyMethodInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Intercepts one proxy method call.
     *
     * @param proxy  the proxy instance
     * @param method the invoked method
     * @param args   the method arguments
     * @return the invocation result
     * @throws Throwable if the invocation fails
     */
    @RuntimeType
    public Object intercept(@This Object proxy, @Origin Method method, @AllArguments Object[] args) throws Throwable {
        return invoker.invoke(proxy, method, args);
    }
}
