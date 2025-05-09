package io.effi.rpc.proxy.bytebuddy;

import io.effi.rpc.proxy.InvocationHandler;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Intercepts and handles method invocations on interfaces and instances.
 */
public class MethodInterceptor implements InvocationHandler {

    private final InvocationHandler invocationHandler;

    public MethodInterceptor(InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, Callable<?> superInvoker) throws Throwable {
        return invocationHandler.invoke(proxy, method, args, superInvoker);
    }

    /**
     * Intercepts interface method invocations.
     */
    public class InterfaceInterceptor {

        @RuntimeType
        public Object intercept(@This Object proxy, @Origin Method method, @AllArguments Object[] args) throws Throwable {
            return invoke(proxy, method, args, () -> null);
        }
    }

    /**
     * Intercepts instance method invocations.
     */
    public class InstanceInterceptor {
        @RuntimeType
        public Object intercept(@This Object proxy, @Origin Method method, @AllArguments Object[] args,
                                @SuperCall Callable<?> callable) throws Throwable {
            return invoke(proxy, method, args, callable);
        }
    }

}