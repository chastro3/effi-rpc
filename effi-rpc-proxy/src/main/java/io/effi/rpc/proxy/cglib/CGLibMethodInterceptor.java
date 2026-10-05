package io.effi.rpc.proxy.cglib;

import io.effi.rpc.proxy.ProxyMethodInvoker;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Adapts CGLib proxy invocations to the framework proxy dispatcher.
 */
public class CGLibMethodInterceptor implements MethodInterceptor {

    private final ProxyMethodInvoker invoker;

    public CGLibMethodInterceptor(ProxyMethodInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return invoker.invoke(obj, method, args);
    }

}
